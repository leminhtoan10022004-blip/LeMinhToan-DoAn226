import React from 'react';
import { UserGroupIcon, ClipboardDocumentIcon, BriefcaseIcon } from '@heroicons/react/24/outline';
import StatCard from '../components/cards/StatCard'; 
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar, Doughnut } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Tooltip,
  Legend
);

const statsData = [
  { title: "Tổng số người dùng", value: 123, unit: "Người", change: "+5% so với tháng trước", icon: UserGroupIcon, iconColor: "bg-orange-100 text-orange-600" },
  { title: "Tổng số bài test", value: 123, unit: "Bài", change: "+5% so với tháng trước", icon: ClipboardDocumentIcon, iconColor: "bg-orange-100 text-orange-600" },
  { title: "Tổng số ngành nghề", value: 123, unit: "Ngành nghề", change: "+5% so với tháng trước", icon: BriefcaseIcon, iconColor: "bg-orange-100 text-orange-600" }
];

const Dashboard = () => {
  const barData = {
    labels: [
        'IT', 'Kinh tế', 'Y dược', 'Sư phạm', 'Cơ khí', 
        'Ngôn ngữ', 'Marketing', 'Luật', 'Du lịch', 'Thiết kế', 'Xây dựng'
      ],
      datasets: [{
        label: 'Số lượng ngành',
        data: [12, 19, 3, 5, 2, 3, 15, 8, 11, 14, 6], 
        backgroundColor: '#f97316', 
        borderRadius: 4,
        barPercentage: 0.6,
      }]
    };

  const donutData = {
    labels: ['MBTI', 'Holland', 'Big Five', 'IQ'],
    datasets: [{
      data: [45, 25, 20, 10],
      backgroundColor: ['#93c5fd', '#86efac', '#fde047', '#fca5a5'],
      hoverOffset: 4,
      borderWidth: 0,
    }]
  };

  return (
    <div className="flex-1 p-6 lg:p-10 bg-gray-50 min-h-screen space-y-8">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {statsData.map((stat, index) => (
          <StatCard key={index} {...stat} Icon={stat.icon} />
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-10 gap-6">
        <div className="lg:col-span-7 bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <h3 className="text-sm font-bold text-gray-700 mb-6 uppercase tracking-wider">
            Thống kê số ngành đã test
          </h3>
          <div className="h-[350px]"> 
            <Bar 
              data={barData} 
              options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: { 
                  y: { beginAtZero: true, grid: { color: '#f3f4f6' } },
                  x: { grid: { display: false } }
                }
              }} 
            />
          </div>
        </div>

        {/* BÊN PHẢI: Biểu đồ Donut (Chiếm 3 phần ~ 30%) */}
        <div className="lg:col-span-3 bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <h3 className="text-sm font-bold text-gray-700 mb-6 uppercase tracking-wider text-center">
            Tỷ lệ bài test
          </h3>
          <div className="h-[350px] flex items-center justify-center">
            <Doughnut 
              data={donutData} 
              options={{
                responsive: true,
                maintainAspectRatio: false,
                cutout: '75%', // Tăng cutout để vòng tròn thanh mảnh, gọn gàng hơn
                plugins: {
                  legend: { 
                    position: 'bottom', 
                    labels: { boxWidth: 12, padding: 15, font: { size: 11 } } 
                  }
                }
              }} 
            />
          </div>
        </div>

      </div>
    </div>
  );
};

export default Dashboard;