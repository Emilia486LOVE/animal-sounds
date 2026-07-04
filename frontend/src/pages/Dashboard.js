import { useState, useEffect } from "react";
import { Card, Row, Col, Statistic, Progress, Table, Tag, Button } from "antd";
import {
  DatabaseOutlined,
  AudioOutlined,
  EditOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  AlertCircleOutlined,
  ArrowUpOutlined,
  UserOutlined,
} from "@ant-design/icons";
import ReactECharts from "echarts-for-react";
import { getOverview } from "../api/statistics";
import { getAllTasks } from "../api/train";
import { getAllDatasets } from "../api/dataset";
import dayjs from "dayjs";

function DashboardPage() {
  const [stats, setStats] = useState({});
  const [loading, setLoading] = useState(false);
  const [tasks, setTasks] = useState([]);
  const [datasets, setDatasets] = useState([]);

  useEffect(() => {
    loadStatistics();
    loadRecentTasks();
    loadDatasets();
  }, []);

  const loadStatistics = () => {
    setLoading(true);
    getOverview()
      .then((res) => {
        setStats(res.data.data || {});
      })
      .finally(() => setLoading(false));
  };

  const loadRecentTasks = () => {
    getAllTasks().then((res) => {
      setTasks(res.data.data.slice(0, 5));
    });
  };

  const loadDatasets = () => {
    getAllDatasets().then((res) => {
      setDatasets(res.data.data.slice(0, 5));
    });
  };

  const statusConfig = {
    pending: { color: "#165DFF", text: "等待中" },
    running: { color: "#FF7D00", text: "训练中" },
    success: { color: "#00B42A", text: "已完成" },
    failed: { color: "#F53F3F", text: "失败" },
  };

  const statCards = [
    {
      title: "数据集总数",
      value: stats.datasetCount || 0,
      icon: DatabaseOutlined,
      color: "#165DFF",
      trend: "+12%",
      trendType: "up",
    },
    {
      title: "音频文件数",
      value: stats.audioCount || 0,
      icon: AudioOutlined,
      color: "#00B42A",
      trend: "+8%",
      trendType: "up",
    },
    {
      title: "标注记录数",
      value: stats.annotationCount || 0,
      icon: EditOutlined,
      color: "#FF7D00",
      trend: "+24%",
      trendType: "up",
    },
    {
      title: "用户总数",
      value: stats.userCount || 0,
      icon: UserOutlined,
      color: "#722ED1",
      trend: "+5%",
      trendType: "up",
    },
    {
      title: "已审核标注",
      value: stats.approvedCount || 0,
      icon: CheckCircleOutlined,
      color: "#00B42A",
      trend: `${(((stats.approvedCount || 0) / Math.max(stats.annotationCount || 1, 1)) * 100).toFixed(1)}%`,
      trendType: "percent",
    },
    {
      title: "待审核标注",
      value: stats.pendingReviewCount || 0,
      icon: ClockCircleOutlined,
      color: "#FF7D00",
      trend: "待处理",
      trendType: "warning",
    },
  ];

  const annotationChartOption = {
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    legend: { data: ["已标注", "待标注", "已审核"] },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: {
      type: "category",
      data: ["哺乳动物", "鸟类", "爬行动物", "两栖动物", "鱼类", "昆虫"],
    },
    yAxis: { type: "value" },
    series: [
      {
        name: "已标注",
        type: "bar",
        data: [120, 85, 45, 30, 60, 95],
        itemStyle: { color: "#165DFF" },
      },
      {
        name: "待标注",
        type: "bar",
        data: [30, 25, 15, 10, 20, 35],
        itemStyle: { color: "#2A3344" },
      },
      {
        name: "已审核",
        type: "bar",
        data: [95, 65, 30, 20, 45, 70],
        itemStyle: { color: "#00B42A" },
      },
    ],
  };

  const trainingTrendOption = {
    tooltip: { trigger: "axis" },
    legend: { data: ["准确率", "F1分数"] },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: [
        "第1轮",
        "第2轮",
        "第3轮",
        "第4轮",
        "第5轮",
        "第6轮",
        "第7轮",
        "第8轮",
      ],
    },
    yAxis: { type: "value", max: 1 },
    series: [
      {
        name: "准确率",
        type: "line",
        data: [0.62, 0.71, 0.76, 0.8, 0.83, 0.85, 0.87, 0.89],
        smooth: true,
        itemStyle: { color: "#165DFF" },
        lineStyle: { width: 3 },
      },
      {
        name: "F1分数",
        type: "line",
        data: [0.58, 0.67, 0.72, 0.77, 0.8, 0.82, 0.84, 0.86],
        smooth: true,
        itemStyle: { color: "#00B42A" },
        lineStyle: { width: 3 },
      },
    ],
  };

  const taskColumns = [
    {
      title: "任务名称",
      dataIndex: "taskName",
      key: "taskName",
      ellipsis: true,
    },
    {
      title: "数据集",
      dataIndex: "datasetId",
      key: "datasetId",
      render: (id) =>
        datasets.find((d) => d.datasetId === id)?.datasetName || id,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (status) => (
        <Tag color={statusConfig[status]?.color}>
          {statusConfig[status]?.text}
        </Tag>
      ),
    },
    {
      title: "最佳指标",
      dataIndex: "bestValMetric",
      key: "bestValMetric",
      render: (val) =>
        val != null ? (
          <span className="number-font">{(val * 100).toFixed(1)}%</span>
        ) : (
          "-"
        ),
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      render: (time) => dayjs(time).format("MM-DD HH:mm"),
    },
  ];

  const datasetColumns = [
    {
      title: "数据集名称",
      dataIndex: "datasetName",
      key: "datasetName",
      ellipsis: true,
    },
    {
      title: "音频数量",
      dataIndex: "audioCount",
      key: "audioCount",
      render: (count) => <span className="number-font">{count || 0}</span>,
    },
    {
      title: "标注进度",
      dataIndex: "audioCount",
      key: "progress",
      render: (count) => {
        const total = count || 0;
        const annotated = Math.floor(total * 0.65);
        return (
          <div>
            <Progress
              percent={total > 0 ? (annotated / total) * 100 : 0}
              showInfo={false}
              strokeColor="#165DFF"
              size={8}
            />
            <span
              className="number-font"
              style={{ fontSize: 12, marginLeft: 8 }}
            >
              {annotated}/{total}
            </span>
          </div>
        );
      },
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      render: (time) => dayjs(time).format("MM-DD"),
    },
  ];

  return (
    <div>
      <div className="page-title" style={{ marginBottom: 20 }}>
        数据看板
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
        {statCards.map((card, index) => (
          <Col span={4} key={index}>
            <Card
              style={{
                backgroundColor: "#1A2233",
                border: "1px solid #2A3344",
                borderRadius: 8,
                borderTop: `3px solid ${card.color}`,
              }}
              styles={{ body: { padding: "16px" } }}
            >
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  marginBottom: 8,
                }}
              >
                <div
                  style={{
                    width: 36,
                    height: 36,
                    borderRadius: 8,
                    backgroundColor: `${card.color}15`,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                >
                  <card.icon style={{ color: card.color, fontSize: 18 }} />
                </div>
                {card.trendType === "up" && (
                  <span
                    style={{
                      color: "#00B42A",
                      fontSize: 12,
                      display: "flex",
                      alignItems: "center",
                    }}
                  >
                    <ArrowUpOutlined style={{ marginRight: 2 }} />
                    {card.trend}
                  </span>
                )}
                {card.trendType === "warning" && (
                  <span style={{ color: "#FF7D00", fontSize: 12 }}>
                    {card.trend}
                  </span>
                )}
                {card.trendType === "percent" && (
                  <span
                    className="number-font"
                    style={{ color: "#00B42A", fontSize: 12 }}
                  >
                    {card.trend}
                  </span>
                )}
              </div>
              <div style={{ color: "#86909C", fontSize: 12, marginBottom: 4 }}>
                {card.title}
              </div>
              <div
                className="number-font"
                style={{ fontSize: 28, fontWeight: 600, color: "#E5E6EB" }}
              >
                {card.value.toLocaleString()}
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]}>
        <Col span={14}>
          <Card
            title={<span className="module-title">分类标注统计</span>}
            style={{
              backgroundColor: "#1A2233",
              border: "1px solid #2A3344",
              borderRadius: 8,
            }}
            styles={{ body: { padding: "20px" } }}
          >
            <ReactECharts
              option={annotationChartOption}
              style={{ height: 300 }}
            />
          </Card>
        </Col>
        <Col span={10}>
          <Card
            title={<span className="module-title">训练趋势</span>}
            style={{
              backgroundColor: "#1A2233",
              border: "1px solid #2A3344",
              borderRadius: 8,
            }}
            styles={{ body: { padding: "20px" } }}
          >
            <ReactECharts
              option={trainingTrendOption}
              style={{ height: 300 }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={14}>
          <Card
            title={<span className="module-title">最近训练任务</span>}
            extra={
              <Button type="text" href="/train">
                查看全部
              </Button>
            }
            style={{
              backgroundColor: "#1A2233",
              border: "1px solid #2A3344",
              borderRadius: 8,
            }}
          >
            <Table
              dataSource={tasks}
              columns={taskColumns}
              rowKey="taskId"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
        <Col span={10}>
          <Card
            title={<span className="module-title">数据集概览</span>}
            extra={
              <Button type="text" href="/dataset">
                查看全部
              </Button>
            }
            style={{
              backgroundColor: "#1A2233",
              border: "1px solid #2A3344",
              borderRadius: 8,
            }}
          >
            <Table
              dataSource={datasets}
              columns={datasetColumns}
              rowKey="datasetId"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card
            title={<span className="module-title">系统状态</span>}
            style={{
              backgroundColor: "#1A2233",
              border: "1px solid #2A3344",
              borderRadius: 8,
            }}
            styles={{ body: { padding: "20px" } }}
          >
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(4, 1fr)",
                gap: 20,
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div
                  style={{
                    width: 10,
                    height: 10,
                    borderRadius: "50%",
                    backgroundColor: "#00B42A",
                  }}
                />
                <div>
                  <div style={{ color: "#E5E6EB", fontSize: 14 }}>
                    数据库连接
                  </div>
                  <div style={{ color: "#00B42A", fontSize: 12 }}>正常</div>
                </div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div
                  style={{
                    width: 10,
                    height: 10,
                    borderRadius: "50%",
                    backgroundColor: "#00B42A",
                  }}
                />
                <div>
                  <div style={{ color: "#E5E6EB", fontSize: 14 }}>
                    文件存储服务
                  </div>
                  <div style={{ color: "#00B42A", fontSize: 12 }}>正常</div>
                </div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div
                  style={{
                    width: 10,
                    height: 10,
                    borderRadius: "50%",
                    backgroundColor: "#FF7D00",
                  }}
                />
                <div>
                  <div style={{ color: "#E5E6EB", fontSize: 14 }}>训练服务</div>
                  <div style={{ color: "#FF7D00", fontSize: 12 }}>空闲中</div>
                </div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div
                  style={{
                    width: 10,
                    height: 10,
                    borderRadius: "50%",
                    backgroundColor: "#00B42A",
                  }}
                />
                <div>
                  <div style={{ color: "#E5E6EB", fontSize: 14 }}>API服务</div>
                  <div style={{ color: "#00B42A", fontSize: 12 }}>正常运行</div>
                </div>
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default DashboardPage;
