import React from 'react';
import { Link } from 'react-router-dom';
import { HomeIcon, QuestionMarkCircleIcon } from '@heroicons/react/24/outline';

const NotFound = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-white p-6 text-center">
      {/* Icon minh họa */}
      <div className="relative mb-8">
        <div className="absolute inset-0 bg-red-100 rounded-full blur-2xl opacity-50 scale-150"></div>
        <QuestionMarkCircleIcon className="relative w-24 h-24 text-red-500 animate-bounce" />
      </div>
      <h1 className="text-9xl font-black text-gray-100 absolute -z-10 select-none">
        404
      </h1>
      
      <div className="z-10">
        <h2 className="text-3xl font-bold text-gray-800 mb-2">
          Ối! Trang này không tồn tại
        </h2>
        <p className="text-gray-500 mb-8 max-w-md mx-auto">
          Có vẻ như đường dẫn bạn đang truy cập đã bị xóa hoặc không còn tồn tại nữa. 
          Hãy quay lại trang chủ để tiếp tục quản lý nhé.
        </p>
        
        <Link
          to="/"
          className="inline-flex items-center gap-2 px-6 py-3 bg-gray-900 text-white rounded-xl font-medium hover:bg-red-600 transition-all duration-300 shadow-lg shadow-gray-200"
        >
          <HomeIcon className="w-5 h-5" />
          Quay về trang chủ
        </Link>
      </div>
    </div>
  );
};

export default NotFound;