// E2E test for dataset audio management module
// Run: node audio-mgmt-test.js  (with NODE_PATH set to global node_modules)
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const BASE = 'http://localhost:3000';
const SHOT_DIR = path.join(__dirname, 'screenshots');
const results = [];

function log(msg) { console.log(`[${new Date().toISOString()}] ${msg}`); }
function record(step, status, detail) {
  results.push({ step, status, detail });
  console.log(`  => [${step}] ${status.toUpperCase()}: ${detail}`);
}

async function shot(page, name) {
  try {
    const file = path.join(SHOT_DIR, name + '.png');
    await page.screenshot({ path: file, fullPage: false });
    log('screenshot saved: ' + file);
    return file;
  } catch (e) {
    log('screenshot failed: ' + e.message);
    return null;
  }
}

// Robust button click that handles Element Plus fixed-column duplication
async function clickButton(scope, name, { exact = false, timeout = 8000 } = {}) {
  const loc = scope.getByRole('button', { name: name, exact });
  const count = await loc.count().catch(() => 0);
  log(`clickButton '${name}': found ${count} candidate(s)`);
  for (let i = 0; i < count; i++) {
    const candidate = loc.nth(i);
    if (await candidate.isVisible().catch(() => false)) {
      try {
        await candidate.click({ timeout });
        log(`  clicked candidate ${i}`);
        return true;
      } catch (e) {
        log(`  candidate ${i} click failed: ${e.message.split('\n')[0]}`);
      }
    }
  }
  if (count > 0) {
    try {
      await loc.last().click({ force: true, timeout });
      log('  force-clicked last candidate');
      return true;
    } catch (e) {
      log('  force click failed: ' + e.message.split('\n')[0]);
    }
  }
  return false;
}

async function exists(scope, selector) {
  try {
    await scope.locator(selector).first().waitFor({ state: 'visible', timeout: 3000 });
    return true;
  } catch {
    return false;
  }
}

async function hasButton(scope, name) {
  const c = await scope.getByRole('button', { name, exact: false }).count().catch(() => 0);
  return c > 0;
}

(async () => {
  fs.mkdirSync(SHOT_DIR, { recursive: true });
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();
  page.setDefaultTimeout(15000);

  let step1Ok = false, step2Ok = false, step3Ok = false, step4Ok = false;
  let step5Ok = false, step6Ok = false, step7Ok = false;

  try {
    // ===== Step 1: Login =====
    log('=== Step 1: Login ===');
    await page.goto(BASE + '/login', { waitUntil: 'networkidle' });
    await page.getByPlaceholder('请输入用户名').fill('admin');
    await page.getByPlaceholder('请输入密码').fill('password');
    await shot(page, '01-login-filled');
    // Login button has aria-label="登录按钮"; accessible name is "登录按钮" not "登录"
    const loginBtn = page.getByRole('button', { name: '登录按钮' });
    if (await loginBtn.count() === 0) {
      // fallback: button containing text 登录
      await page.locator('button.login-btn, button:has-text("登录")').first().click();
    } else {
      await loginBtn.click();
    }
    try {
      await page.waitForURL('**/dashboard', { timeout: 15000 });
      step1Ok = true;
      record('Step1-登录', 'pass', '使用 admin/password 登录成功，跳转到 /dashboard');
    } catch (e) {
      // maybe already redirected or message error
      const url = page.url();
      if (url.includes('/dashboard')) {
        step1Ok = true;
        record('Step1-登录', 'pass', '登录成功，已处于 /dashboard');
      } else {
        await shot(page, '01-login-fail');
        record('Step1-登录', 'fail', `登录后未跳转到 dashboard，当前 URL: ${url}`);
      }
    }
    await shot(page, '02-dashboard');

    // ===== Step 2: Click "数据集管理" menu =====
    log('=== Step 2: Click dataset menu ===');
    try {
      // wait for sidebar menu to appear (loading overlay gone)
      await page.locator('.el-menu-item').first().waitFor({ state: 'visible', timeout: 15000 });
      const menuItem = page.locator('.el-menu-item', { hasText: '数据集管理' }).first();
      await menuItem.waitFor({ state: 'visible', timeout: 10000 });
      await menuItem.click();
      await page.waitForURL('**/dataset', { timeout: 15000 });
      // wait for dataset table
      await page.locator('.el-table').first().waitFor({ state: 'visible', timeout: 15000 });
      step2Ok = true;
      record('Step2-进入数据集管理', 'pass', '点击"数据集管理"菜单，跳转到 /dataset，表格已渲染');
    } catch (e) {
      await shot(page, '02-dataset-fail');
      record('Step2-进入数据集管理', 'fail', '进入数据集管理失败: ' + e.message.split('\n')[0]);
    }
    await shot(page, '03-dataset-list');

    // ===== Step 3: Check dataset list & 4 buttons per row =====
    log('=== Step 3: Check dataset list & row buttons ===');
    let datasetCount = 0;
    let firstDatasetName = '';
    try {
      // wait for loading to finish and rows to appear
      await page.waitForTimeout(1500);
      const rows = page.locator('.el-table__body-wrapper .el-table__row');
      datasetCount = await rows.count();
      log(`dataset rows found: ${datasetCount}`);
      if (datasetCount === 0) {
        record('Step3-数据集列表', 'fail', '数据集列表为空，没有数据行');
      } else {
        // get first dataset name from first cell
        const firstRow = rows.first();
        firstDatasetName = (await firstRow.locator('td').first().innerText()).trim();
        log(`first dataset name: ${firstDatasetName}`);
        const btnNames = ['管理音频', '编辑', '刷新数量', '删除'];
        let allPresent = true;
        const details = [];
        for (const n of btnNames) {
          const c = await firstRow.getByRole('button', { name: n, exact: true }).count();
          details.push(`${n}=${c}`);
          if (c === 0) allPresent = false;
        }
        step3Ok = allPresent;
        record('Step3-数据集列表与按钮', allPresent ? 'pass' : 'fail',
          `共 ${datasetCount} 个数据集，首行"${firstDatasetName}"。按钮: ${details.join(', ')}`);
      }
    } catch (e) {
      record('Step3-数据集列表', 'fail', '检查异常: ' + e.message.split('\n')[0]);
    }
    await shot(page, '04-dataset-buttons');

    // ===== Step 4: Click "管理音频" on first dataset =====
    log('=== Step 4: Click 管理音频 ===');
    let audioDialog = null;
    try {
      if (datasetCount === 0) {
        record('Step4-管理音频', 'skip', '无数据集，跳过');
      } else {
        const ok = await clickButton(page, '管理音频', { exact: true });
        if (ok) {
          // wait for audio management dialog
          const dialog = page.locator('.el-dialog').filter({ hasText: '音频管理' }).last();
          await dialog.waitFor({ state: 'visible', timeout: 10000 });
          audioDialog = dialog;
          step4Ok = true;
          record('Step4-管理音频', 'pass', '点击首行"管理音频"按钮，音频管理对话框已弹出');
        } else {
          record('Step4-管理音频', 'fail', '未找到可点击的"管理音频"按钮');
        }
      }
    } catch (e) {
      record('Step4-管理音频', 'fail', '点击异常: ' + e.message.split('\n')[0]);
    }
    await shot(page, '05-audio-dialog');

    // ===== Step 5: Check audio management dialog content =====
    log('=== Step 5: Check audio dialog content ===');
    try {
      if (!audioDialog) {
        record('Step5-音频对话框内容', 'skip', '对话框未打开，跳过');
      } else {
        // 5a title contains dataset name
        const titleText = await audioDialog.locator('.el-dialog__title').innerText().catch(() => '');
        const titleHasName = firstDatasetName ? titleText.includes(firstDatasetName) : titleText.includes('音频管理');
        record('Step5a-标题包含数据集名称', titleHasName ? 'pass' : 'fail',
          `对话框标题: "${titleText}"${firstDatasetName ? '，期望包含: ' + firstDatasetName : ''}`);

        // 5b audio file list (table) with columns: 文件名、时长、采样率、噪声等级
        const audioRows = audioDialog.locator('.el-table__body-wrapper .el-table__row');
        const audioRowCount = await audioRows.count();
        // table header columns
        const headerText = await audioDialog.locator('.el-table__header-wrapper').innerText().catch(() => '');
        const hasFileName = /文件名/.test(headerText);
        const hasDuration = /时长/.test(headerText);
        const hasSampleRate = /采样率/.test(headerText);
        const hasNoise = /噪声等级/.test(headerText);
        const colsOk = hasFileName && hasDuration && hasSampleRate && hasNoise;
        record('Step5b-音频文件列表与列', colsOk ? 'pass' : 'fail',
          `音频行数: ${audioRowCount}；列: 文件名=${hasFileName}, 时长=${hasDuration}, 采样率=${hasSampleRate}, 噪声等级=${hasNoise}`);

        // 5c 上传音频 button
        const hasUpload = await hasButton(audioDialog, '上传音频');
        // 5d 批量转移 / 批量删除 buttons
        const hasBatchMove = await hasButton(audioDialog, '批量转移');
        const hasBatchDel = await hasButton(audioDialog, '批量删除');
        record('Step5c-上传音频按钮', hasUpload ? 'pass' : 'fail', hasUpload ? '存在"上传音频"按钮' : '未找到"上传音频"按钮');
        record('Step5d-批量操作按钮', (hasBatchMove && hasBatchDel) ? 'pass' : 'fail',
          `批量转移=${hasBatchMove}, 批量删除=${hasBatchDel}`);

        // 5e per-row 转移/删除 buttons
        let rowOpsOk = false;
        if (audioRowCount > 0) {
          const firstAudioRow = audioRows.first();
          const hasMove = await hasButton(firstAudioRow, '转移');
          const hasDel = await hasButton(firstAudioRow, '删除');
          rowOpsOk = hasMove && hasDel;
          record('Step5e-每行转移/删除按钮', rowOpsOk ? 'pass' : 'fail',
            `首行: 转移=${hasMove}, 删除=${hasDel}`);
        } else {
          record('Step5e-每行转移/删除按钮', 'skip', '无音频行，无法验证行内按钮');
        }

        step5Ok = titleHasName && colsOk && hasUpload && hasBatchMove && hasBatchDel && (audioRowCount === 0 || rowOpsOk);
      }
    } catch (e) {
      record('Step5-音频对话框内容', 'fail', '检查异常: ' + e.message.split('\n')[0]);
    }
    await shot(page, '06-audio-dialog-content');

    // ===== Step 6: Select a few audio files, check batch count =====
    log('=== Step 6: Select audio files & check batch count ===');
    let selectedCount = 0;
    try {
      if (!audioDialog) {
        record('Step6-选择音频与批量计数', 'skip', '对话框未打开，跳过');
      } else {
        const audioRows = audioDialog.locator('.el-table__body-wrapper .el-table__row');
        const total = await audioRows.count();
        if (total === 0) {
          record('Step6-选择音频与批量计数', 'skip', '该数据集无音频文件，无法选择');
        } else {
          // select up to 3 (or fewer)
          const toSelect = Math.min(3, total);
          for (let i = 0; i < toSelect; i++) {
            const cb = audioRows.nth(i).locator('.el-checkbox').first();
            await cb.click({ timeout: 5000 });
            log(`  selected row ${i}`);
            selectedCount++;
          }
          await page.waitForTimeout(500);
          await shot(page, '07-audio-selected');

          // read batch button texts
          const batchMoveText = await audioDialog.getByRole('button', { name: /批量转移/ }).first().innerText().catch(() => '');
          const batchDelText = await audioDialog.getByRole('button', { name: /批量删除/ }).first().innerText().catch(() => '');
          log(`  batch move text: "${batchMoveText}", batch del text: "${batchDelText}"`);
          const moveMatch = batchMoveText.match(/批量转移\s*\((\d+)\)/);
          const delMatch = batchDelText.match(/批量删除\s*\((\d+)\)/);
          const moveNum = moveMatch ? parseInt(moveMatch[1]) : -1;
          const delNum = delMatch ? parseInt(delMatch[1]) : -1;
          const countOk = moveNum === selectedCount && delNum === selectedCount;
          step6Ok = countOk;
          record('Step6-选择音频与批量计数', countOk ? 'pass' : 'fail',
            `选中 ${selectedCount} 个，批量转移显示(${moveNum})，批量删除显示(${delNum})`);
        }
      }
    } catch (e) {
      record('Step6-选择音频与批量计数', 'fail', '操作异常: ' + e.message.split('\n')[0]);
    }
    await shot(page, '08-batch-count');

    // ===== Step 7: Click 刷新数量, check success =====
    log('=== Step 7: 刷新数量 ===');
    try {
      // close audio dialog first (刷新数量 is on dataset list)
      if (audioDialog && await audioDialog.isVisible().catch(() => false)) {
        const closeBtn = page.locator('.el-dialog__headerbtn').last();
        if (await closeBtn.isVisible().catch(() => false)) {
          await closeBtn.click({ timeout: 5000 }).catch(() => {});
        } else {
          await page.keyboard.press('Escape');
        }
        await page.waitForTimeout(800);
        log('  closed audio dialog');
      }
      await shot(page, '09-back-to-dataset-list');

      if (datasetCount === 0) {
        record('Step7-刷新数量', 'skip', '无数据集，跳过');
      } else {
        // capture audio count before
        const firstRow = page.locator('.el-table__body-wrapper .el-table__row').first();
        // audio count is the 3rd column (index 2): 数据集名称, 描述, 音频数量
        const beforeCells = await firstRow.locator('td').allInnerTexts();
        const beforeCount = beforeCells[2] ? beforeCells[2].trim() : '?';
        log(`  audio count before refresh: ${beforeCount}`);

        const ok = await clickButton(page, '刷新数量', { exact: true });
        if (!ok) {
          record('Step7-刷新数量', 'fail', '未找到"刷新数量"按钮');
        } else {
          // wait for success message
          let success = false;
          try {
            const msg = page.locator('.el-message--success', { hasText: '音频数量已刷新' });
            await msg.waitFor({ state: 'visible', timeout: 10000 });
            success = true;
          } catch {
            // maybe just .el-message with success
            try {
              const msg2 = page.locator('.el-message').filter({ hasText: '刷新' });
              await msg2.waitFor({ state: 'visible', timeout: 5000 });
              success = true;
            } catch {}
          }
          await page.waitForTimeout(800);
          await shot(page, '10-refresh-count-result');
          // read count after
          const afterCells = await page.locator('.el-table__body-wrapper .el-table__row').first().locator('td').allInnerTexts();
          const afterCount = afterCells[2] ? afterCells[2].trim() : '?';
          log(`  audio count after refresh: ${afterCount}`);
          step7Ok = success;
          record('Step7-刷新数量', success ? 'pass' : 'fail',
            success ? `刷新成功，提示"音频数量已刷新"，刷新前数量=${beforeCount}，刷新后数量=${afterCount}`
                    : `未出现成功提示（刷新前=${beforeCount}，刷新后=${afterCount}）`);
        }
      }
    } catch (e) {
      record('Step7-刷新数量', 'fail', '操作异常: ' + e.message.split('\n')[0]);
    }
    await shot(page, '11-final');

  } catch (e) {
    log('FATAL: ' + e.message);
    record('Fatal', 'fail', e.message);
    await shot(page, 'fatal').catch(() => {});
  } finally {
    await browser.close();
  }

  // summary
  const summary = {
    steps: results,
    overall: {
      step1_login: step1Ok,
      step2_menu: step2Ok,
      step3_list_buttons: step3Ok,
      step4_open_audio: step4Ok,
      step5_dialog_content: step5Ok,
      step6_select_batch: step6Ok,
      step7_refresh_count: step7Ok,
    }
  };
  fs.writeFileSync(path.join(__dirname, 'test-result.json'), JSON.stringify(summary, null, 2), 'utf-8');
  log('=== TEST SUMMARY ===');
  for (const r of results) {
    console.log(`[${r.status.toUpperCase()}] ${r.step}: ${r.detail}`);
  }
  log('Result JSON saved to: ' + path.join(__dirname, 'test-result.json'));
  process.exit(0);
})();
