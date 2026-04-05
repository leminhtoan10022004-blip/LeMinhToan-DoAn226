import React, { useState } from 'react';
import { 
  ChevronDownIcon, 
  ChevronUpIcon, 
  PlusIcon, 
  PencilSquareIcon, 
  TrashIcon,
  CheckCircleIcon
} from '@heroicons/react/24/outline';

const QuestionManagement = () => {
  const [expandedId, setExpandedId] = useState(null);

  // Dữ liệu mẫu kết hợp bảng CauHoi và DapAn từ ERD
  const [questions, setQuestions] = useState([
    {
      id: 1,
      content: "Bạn thích làm việc với các thiết bị máy móc hay phần mềm?",
      order: 1,
      answers: [
        { id: 101, text: "Rất thích", isCorrect: true, point: 10 },
        { id: 102, text: "Bình thường", isCorrect: false, point: 5 },
        { id: 103, text: "Không thích", isCorrect: false, point: 0 },
      ]
    },
    {
      id: 2,
      content: "Bạn có khả năng thuyết phục người khác không?",
      order: 2,
      answers: [
        { id: 201, text: "Có, rất tự tin", isCorrect: true, point: 10 },
        { id: 202, text: "Đôi khi", isCorrect: false, point: 5 },
      ]
    }
  ]);

  const toggleExpand = (id) => {
    setExpandedId(expandedId === id ? null : id);
  };

  return (
    <div className="flex-1 p-6 lg:p-10 bg-gray-50 min-h-screen">
      {/* Header điều hướng quay lại bài test */}
      <div className="mb-8 flex justify-between items-end">
        <div>
          <nav className="flex text-sm text-gray-400 mb-2 gap-2">
            <span className="hover:text-orange-500 cursor-pointer">Bài Test</span>
            <span>/</span>
            <span className="text-gray-800 font-medium">Trắc nghiệm Holland</span>
          </nav>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">Danh sách câu hỏi</h1>
        </div>
        <button className="flex items-center gap-2 px-6 py-3 bg-orange-500 text-white rounded-2xl text-sm font-bold hover:bg-orange-600 transition-all shadow-lg shadow-orange-100">
          <PlusIcon className="w-5 h-5" />
          Thêm câu hỏi mới
        </button>
      </div>

      {/* Danh sách câu hỏi dạng Accordion để tạo độ thoáng */}
      <div className="space-y-4">
        {questions.map((q) => (
          <div key={q.id} className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden transition-all">
            {/* Phần thanh tiêu đề câu hỏi */}
            <div 
              className={`p-6 flex items-center justify-between cursor-pointer hover:bg-gray-50/50 ${expandedId === q.id ? 'bg-orange-50/30' : ''}`}
              onClick={() => toggleExpand(q.id)}
            >
              <div className="flex items-center gap-6">
                <span className="w-10 h-10 flex items-center justify-center bg-white border border-gray-100 rounded-xl text-orange-600 font-bold shadow-sm">
                  {q.order}
                </span>
                <p className="font-bold text-gray-700 text-lg">{q.content}</p>
              </div>
              <div className="flex items-center gap-4">
                <div className="flex gap-1">
                  <button className="p-2 text-gray-400 hover:text-green-500 hover:bg-green-50 rounded-lg transition-all">
                    <PencilSquareIcon className="w-5 h-5" />
                  </button>
                  <button className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all">
                    <TrashIcon className="w-5 h-5" />
                  </button>
                </div>
                {expandedId === q.id ? <ChevronUpIcon className="w-5 h-5 text-gray-400" /> : <ChevronDownIcon className="w-5 h-5 text-gray-400" />}
              </div>
            </div>

            {/* Phần chi tiết đáp án khi mở rộng */}
            {expandedId === q.id && (
              <div className="p-8 bg-white border-t border-gray-50 animate-fadeIn">
                <div className="flex justify-between items-center mb-6">
                  <h4 className="text-[11px] uppercase tracking-widest font-bold text-gray-400">Danh sách đáp án</h4>
                  <button className="text-sm font-bold text-orange-600 hover:underline flex items-center gap-1">
                    <PlusIcon className="w-4 h-4" /> Thêm đáp án
                  </button>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {q.answers.map((ans) => (
                    <div key={ans.id} className="flex items-center justify-between p-4 rounded-2xl border border-gray-50 bg-gray-50/30 hover:border-orange-200 transition-all">
                      <div className="flex items-center gap-3">
                        {ans.isCorrect ? <CheckCircleIcon className="w-5 h-5 text-green-500" /> : <div className="w-5 h-5 rounded-full border-2 border-gray-200" />}
                        <span className="text-gray-700 font-medium">{ans.text}</span>
                      </div>
                      <span className="text-xs font-bold text-gray-400 bg-white px-2 py-1 rounded-lg shadow-sm">
                        {ans.point} Điểm
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default QuestionManagement;