import React, { useState } from 'react';
import { 
  MagnifyingGlassIcon, 
  ArrowDownTrayIcon,
  EyeIcon,
  TrashIcon,
  CheckCircleIcon,
  ClockIcon
} from '@heroicons/react/24/outline';

const TestHistory = () => {
  const [history, setHistory] = useState([
    { 
      id: "LS001", 
      userName: "Lê Minh Toàn", 
      testName: "Trắc nghiệm Holland", 
      startTime: "2026-03-22 08:30", 
      endTime: "2026-03-22 08:50",
      status: "Hoàn thành",
      result: "Kỹ thuật (R)" 
    },
    { 
      id: "LS002", 
      userName: "Nguyễn Văn A", 
      testName: "Kiểm tra IQ", 
      startTime: "2026-03-22 09:00", 
      endTime: "---",
      status: "Đang làm",
      result: "---" 
    },
    { 
      id: "LS003", 
      userName: "Trần Thị B", 
      testName: "Tính cách MBTI", 
      startTime: "2026-03-21 14:20", 
      endTime: "2026-03-21 14:55",
      status: "Hoàn thành",
      result: "ENTJ" 
    },
  ]);

  return (
    <div className="flex-1 p-6 lg:p-10 bg-gray-50 min-h-screen">
      {/* Tiêu đề trang */}
      <div className="mb-8 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">Lịch sử làm bài</h1>
          <p className="text-gray-500 text-sm mt-1">Theo dõi quá trình và kết quả thực hiện bài test của người dùng.</p>
        </div>
        <button className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 text-gray-600 rounded-xl text-sm font-medium hover:bg-gray-50 transition-all shadow-sm">
          <ArrowDownTrayIcon className="w-4 h-4" />
          Xuất báo cáo
        </button>
      </div>

      {/* Bộ lọc nhanh */}
      <div className="bg-white p-4 rounded-2xl border border-gray-100 shadow-sm mb-6 flex flex-wrap gap-4 items-center">
        <div className="relative flex-1 min-w-[300px]">
          <MagnifyingGlassIcon className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input 
            type="text" 
            placeholder="Tìm theo tên người dùng hoặc bài test..." 
            className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border-none rounded-xl text-sm focus:ring-2 focus:ring-orange-500 transition-all"
          />
        </div>
        <select className="bg-gray-50 border-none rounded-xl text-sm py-2.5 px-4 text-gray-600 focus:ring-2 focus:ring-orange-500">
          <option>Tất cả trạng thái</option>
          <option>Hoàn thành</option>
          <option>Đang làm</option>
        </select>
      </div>

      {/* Bảng lịch sử */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50/50 text-gray-400 text-[11px] uppercase tracking-widest font-bold">
              <th className="px-6 py-4">Người thực hiện</th>
              <th className="px-6 py-4">Tên bài test</th>
              <th className="px-6 py-4 text-center">Thời gian</th>
              <th className="px-6 py-4">Trạng thái</th>
              <th className="px-6 py-4">Kết quả</th>
              <th className="px-6 py-4 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50 text-sm">
            {history.map((item) => (
              <tr key={item.id} className="hover:bg-gray-50/50 transition-colors">
                <td className="px-6 py-4 font-semibold text-gray-800">{item.userName}</td>
                <td className="px-6 py-4 text-gray-600 font-medium">{item.testName}</td>
                <td className="px-6 py-4">
                  <div className="text-[12px] flex flex-col items-center">
                    <span className="text-gray-400 flex items-center gap-1">
                      <ClockIcon className="w-3 h-3" /> Bắt đầu: {item.startTime}
                    </span>
                    <span className="text-gray-500 font-medium">Kết thúc: {item.endTime}</span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className={`flex items-center gap-1.5 w-fit px-3 py-1 rounded-full text-[10px] font-bold uppercase ${
                    item.status === 'Hoàn thành' ? 'bg-green-100 text-green-600' : 'bg-blue-100 text-blue-600'
                  }`}>
                    {item.status === 'Hoàn thành' ? <CheckCircleIcon className="w-3 h-3" /> : <ClockIcon className="w-3 h-3" />}
                    {item.status}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <span className="text-orange-600 font-bold">{item.result}</span>
                </td>
                <td className="px-6 py-4 text-right">
                  <div className="flex justify-end gap-2">
                    <button className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-all" title="Xem chi tiết câu trả lời">
                      <EyeIcon className="w-5 h-5" />
                    </button>
                    <button className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-all" title="Xóa lịch sử">
                      <TrashIcon className="w-5 h-5" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TestHistory;