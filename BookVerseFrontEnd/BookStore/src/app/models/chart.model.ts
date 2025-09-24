export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string[];
    borderColor?: string;
    borderWidth?: number;
  }[];
}

export interface PieChartData {
  labels: string[];
  data: number[];
  backgroundColor?: string[];
} 