import { useState, useEffect } from "react";
import {
  Table,
  Button,
  Select,
  Tag,
  Card,
  Statistic,
  Row,
  Col,
  message,
} from "antd";
import { BarChartOutlined, LineChartOutlined } from "@ant-design/icons";
import ReactECharts from "echarts-for-react";
import { getAllTasks } from "../api/train";
import {
  getEvaluationsByTaskId,
  getTaskEvaluationSummary,
} from "../api/evaluation";
import dayjs from "dayjs";

function EvaluationPage() {
  const [tasks, setTasks] = useState([]);
  const [selectedTask, setSelectedTask] = useState(null);
  const [evaluations, setEvaluations] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadTasks();
  }, []);

  useEffect(() => {
    if (selectedTask) {
      loadEvaluations(selectedTask);
    }
  }, [selectedTask]);

  const loadTasks = () => {
    getAllTasks().then((res) => {
      const completedTasks = res.data.data.filter(
        (t) => t.status === "success",
      );
      setTasks(completedTasks);
    });
  };

  const loadEvaluations = (taskId) => {
    setLoading(true);
    Promise.all([
      getEvaluationsByTaskId(taskId),
      getTaskEvaluationSummary(taskId),
    ])
      .then(([evalRes, summaryRes]) => {
        setEvaluations(evalRes.data.data || []);
        setSummary(summaryRes.data.data || null);
      })
      .catch((err) => {
        message.error("加载评估数据失败");
      })
      .finally(() => setLoading(false));
  };

  const handleTaskChange = (taskId) => {
    setSelectedTask(taskId);
  };

  const rankNames = {
    kingdom: "界",
    phylum: "门",
    class: "纲",
    order: "目",
    family: "科",
    genus: "属",
    species: "种",
  };

  const columns = [
    {
      title: "分类层级",
      dataIndex: "taxonRank",
      key: "taxonRank",
      render: (rank) => rankNames[rank] || rank,
    },
    {
      title: "准确率",
      dataIndex: "accuracy",
      key: "accuracy",
      render: (val) =>
        val != null ? (
          <span className="number-font">{(val * 100).toFixed(2)}%</span>
        ) : (
          "-"
        ),
    },
    {
      title: "精确率",
      dataIndex: "precision",
      key: "precision",
      render: (val) =>
        val != null ? (
          <span className="number-font">{(val * 100).toFixed(2)}%</span>
        ) : (
          "-"
        ),
    },
    {
      title: "召回率",
      dataIndex: "recall",
      key: "recall",
      render: (val) =>
        val != null ? (
          <span className="number-font">{(val * 100).toFixed(2)}%</span>
        ) : (
          "-"
        ),
    },
    {
      title: "F1分数",
      dataIndex: "f1Score",
      key: "f1Score",
      render: (val) =>
        val != null ? (
          <span className="number-font">{(val * 100).toFixed(2)}%</span>
        ) : (
          "-"
        ),
    },
  ];

  const accuracyChartOption = {
    title: {
      text: "各层级准确率对比",
      left: "center",
      textStyle: { color: "#E5E6EB" },
    },
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: {
      type: "category",
      data: evaluations.map((e) => rankNames[e.taxonRank] || e.taxonRank),
      axisLabel: { color: "#86909C" },
    },
    yAxis: { type: "value", max: 1, axisLabel: { color: "#86909C" } },
    series: [
      {
        name: "准确率",
        type: "bar",
        data: evaluations.map((e) => e.accuracy),
        itemStyle: { color: "#165DFF" },
      },
    ],
  };

  const f1ChartOption = {
    title: {
      text: "各层级F1分数对比",
      left: "center",
      textStyle: { color: "#E5E6EB" },
    },
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: {
      type: "category",
      data: evaluations.map((e) => rankNames[e.taxonRank] || e.taxonRank),
      axisLabel: { color: "#86909C" },
    },
    yAxis: { type: "value", max: 1, axisLabel: { color: "#86909C" } },
    series: [
      {
        name: "F1分数",
        type: "line",
        data: evaluations.map((e) => e.f1Score),
        smooth: true,
        itemStyle: { color: "#165DFF" },
        lineStyle: { width: 3 },
      },
    ],
  };

  const metricsChartOption = {
    title: {
      text: "各层级评估指标对比",
      left: "center",
      textStyle: { color: "#E5E6EB" },
    },
    tooltip: { trigger: "axis" },
    legend: {
      data: ["准确率", "精确率", "召回率", "F1分数"],
      textStyle: { color: "#86909C" },
    },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: {
      type: "category",
      data: evaluations.map((e) => rankNames[e.taxonRank] || e.taxonRank),
      axisLabel: { color: "#86909C" },
    },
    yAxis: { type: "value", max: 1, axisLabel: { color: "#86909C" } },
    series: [
      {
        name: "准确率",
        type: "line",
        data: evaluations.map((e) => e.accuracy),
        smooth: true,
        itemStyle: { color: "#165DFF" },
      },
      {
        name: "精确率",
        type: "line",
        data: evaluations.map((e) => e.precision),
        smooth: true,
        itemStyle: { color: "#00B42A" },
      },
      {
        name: "召回率",
        type: "line",
        data: evaluations.map((e) => e.recall),
        smooth: true,
        itemStyle: { color: "#FF7D00" },
      },
      {
        name: "F1分数",
        type: "line",
        data: evaluations.map((e) => e.f1Score),
        smooth: true,
        itemStyle: { color: "#722ED1" },
      },
    ],
  };

  const selectedTaskInfo = tasks.find((t) => t.taskId === selectedTask);

  return (
    <div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 20,
        }}
      >
        <div className="page-title">模型评估结果</div>
        <Select
          placeholder="选择训练任务"
          value={selectedTask}
          onChange={handleTaskChange}
          style={{ width: 250 }}
        >
          {tasks.map((t) => (
            <Select.Option key={t.taskId} value={t.taskId}>
              {t.taskName} ({dayjs(t.endTime).format("MM-DD HH:mm")})
            </Select.Option>
          ))}
        </Select>
      </div>

      {selectedTaskInfo && (
        <Card
          style={{
            marginBottom: 20,
            backgroundColor: "#1A2233",
            border: "1px solid #2A3344",
            borderRadius: 8,
          }}
        >
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <div>
              <h3 className="module-title">{selectedTaskInfo.taskName}</h3>
              <p style={{ color: "#86909C", fontSize: 12 }}>
                模型类型: {selectedTaskInfo.modelType} | 完成时间:{" "}
                {dayjs(selectedTaskInfo.endTime).format("YYYY-MM-DD HH:mm")}
              </p>
            </div>
            <Tag color="green">评估完成</Tag>
          </div>
        </Card>
      )}

      {summary && (
        <Row gutter={16} style={{ marginBottom: 20 }}>
          <Col span={6}>
            <Card
              style={{
                backgroundColor: "#1A2233",
                border: "1px solid #2A3344",
                borderRadius: 8,
                borderTop: "3px solid #165DFF",
              }}
            >
              <Statistic
                title="平均准确率"
                value={(summary.avgAccuracy || 0) * 100}
                suffix="%"
                precision={2}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card
              style={{
                backgroundColor: "#1A2233",
                border: "1px solid #2A3344",
                borderRadius: 8,
                borderTop: "3px solid #00B42A",
              }}
            >
              <Statistic
                title="平均精确率"
                value={(summary.avgPrecision || 0) * 100}
                suffix="%"
                precision={2}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card
              style={{
                backgroundColor: "#1A2233",
                border: "1px solid #2A3344",
                borderRadius: 8,
                borderTop: "3px solid #FF7D00",
              }}
            >
              <Statistic
                title="平均召回率"
                value={(summary.avgRecall || 0) * 100}
                suffix="%"
                precision={2}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card
              style={{
                backgroundColor: "#1A2233",
                border: "1px solid #2A3344",
                borderRadius: 8,
                borderTop: "3px solid #722ED1",
              }}
            >
              <Statistic
                title="平均F1分数"
                value={(summary.avgF1Score || 0) * 100}
                suffix="%"
                precision={2}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Row gutter={16}>
        <Col span={12}>
          <Card
            title={
              <span>
                <BarChartOutlined /> 准确率柱状图
              </span>
            }
            style={{
              backgroundColor: "#1A2233",
              border: "1px solid #2A3344",
              borderRadius: 8,
            }}
          >
            {evaluations.length > 0 ? (
              <ReactECharts
                option={accuracyChartOption}
                style={{ height: 300 }}
              />
            ) : (
              <div
                style={{
                  textAlign: "center",
                  padding: "60px 0",
                  color: "#646D7A",
                }}
              >
                请选择一个已完成的训练任务
              </div>
            )}
          </Card>
        </Col>
        <Col span={12}>
          <Card
            title={
              <span>
                <LineChartOutlined /> F1分数折线图
              </span>
            }
            style={{
              backgroundColor: "#1A2233",
              border: "1px solid #2A3344",
              borderRadius: 8,
            }}
          >
            {evaluations.length > 0 ? (
              <ReactECharts option={f1ChartOption} style={{ height: 300 }} />
            ) : (
              <div
                style={{
                  textAlign: "center",
                  padding: "60px 0",
                  color: "#646D7A",
                }}
              >
                请选择一个已完成的训练任务
              </div>
            )}
          </Card>
        </Col>
      </Row>

      <Card
        title="各层级评估指标对比"
        style={{
          marginTop: 16,
          backgroundColor: "#1A2233",
          border: "1px solid #2A3344",
          borderRadius: 8,
        }}
      >
        {evaluations.length > 0 ? (
          <ReactECharts option={metricsChartOption} style={{ height: 400 }} />
        ) : (
          <div
            style={{ textAlign: "center", padding: "60px 0", color: "#646D7A" }}
          >
            请选择一个已完成的训练任务
          </div>
        )}
      </Card>

      <Card
        title="详细评估数据"
        style={{
          marginTop: 16,
          backgroundColor: "#1A2233",
          border: "1px solid #2A3344",
          borderRadius: 8,
        }}
      >
        <Table
          dataSource={evaluations}
          columns={columns}
          rowKey="evalId"
          loading={loading}
          pagination={false}
        />
      </Card>
    </div>
  );
}

export default EvaluationPage;
