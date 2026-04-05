import React, { useState } from "react";
import { Link } from "react-router-dom";
import {
  PlusIcon,
  PencilSquareIcon,
  TrashIcon,
  ListBulletIcon,
  ClockIcon,
  DocumentTextIcon,
} from "@heroicons/react/24/outline";

const TestManagement = () => {
  // Dữ liệu mẫu dựa trên bảng BaiTest
  const [tests, setTests] = useState([
    {
      id: 1,
      title: "Trắc nghiệm Holland",
      duration: 20,
      questions: 60,
      type: "Hướng nghiệp",
    },
    {
      id: 2,
      title: "Tính cách MBTI",
      duration: 30,
      questions: 70,
      type: "Tính cách",
    },
  ]);

  return (
    <div className="flex-1 p-6 lg:p-10 bg-gray-50 min-h-screen">
      <div className="flex justify-between items-center mb-10">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">
            Quản lý Bài Test
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            Thiết lập bộ câu hỏi và thời gian làm bài.
          </p>
        </div>
        <button className="flex items-center gap-2 px-6 py-3 bg-orange-500 text-white rounded-2xl text-sm font-bold hover:bg-orange-600 transition-all shadow-lg shadow-orange-100">
          <PlusIcon className="w-5 h-5" />
          Tạo bài test mới
        </button>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
        {tests.map((test) => (
          <div
            key={test.id}
            className="bg-white p-8 rounded-[2rem] border border-gray-100 shadow-sm hover:shadow-md transition-all group"
          >
            <div className="flex justify-between items-start mb-6">
              <div className="p-3 bg-orange-50 rounded-2xl text-orange-600">
                <DocumentTextIcon className="w-8 h-8" />
              </div>
              <div className="flex gap-2">
                <button
                  className="p-2 text-gray-400 hover:text-green-500 hover:bg-green-50 rounded-xl transition-all"
                  title="Sửa thông tin bài test"
                >
                  <PencilSquareIcon className="w-5 h-5" />
                </button>
                <button
                  className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all"
                  title="Xóa bài test"
                >
                  <TrashIcon className="w-5 h-5" />
                </button>
              </div>
            </div>

            <h3 className="text-xl font-bold text-gray-800 mb-2">
              {test.title}
            </h3>
            <p className="text-gray-400 text-sm mb-6">
              Loại:{" "}
              <span className="text-orange-500 font-medium">{test.type}</span>
            </p>

            <div className="grid grid-cols-2 gap-4 py-4 border-t border-gray-50">
              <div className="flex items-center gap-3">
                <ClockIcon className="w-5 h-5 text-gray-400" />
                <div>
                  <p className="text-[10px] uppercase font-bold text-gray-400 tracking-widest">
                    Thời gian
                  </p>
                  <p className="text-sm font-bold text-gray-700">
                    {test.duration} Phút
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <ListBulletIcon className="w-5 h-5 text-gray-400" />
                <div>
                  <p className="text-[10px] uppercase font-bold text-gray-400 tracking-widest">
                    Số câu hỏi
                  </p>
                  <p className="text-sm font-bold text-gray-700">
                    {test.questions} Câu
                  </p>
                </div>
              </div>
            </div>

            <Link
              to={`/tests/${test.id}/questions`} 
              className="w-full mt-6 py-4 bg-gray-50 text-gray-600 font-bold rounded-2xl hover:bg-orange-500 hover:text-white transition-all flex justify-center items-center gap-2"
            >
              <ListBulletIcon className="w-5 h-5" />
              Quản lý danh sách câu hỏi
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
};

export default TestManagement;
