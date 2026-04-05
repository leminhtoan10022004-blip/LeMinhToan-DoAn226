import React from 'react';

const StatCard = ({ title, value, unit, change, Icon, iconColor }) => {
  return (
    <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-all duration-300">
      
      <div className="flex items-start gap-3 mb-3">
        <div className={`p-2.5 rounded-xl ${iconColor || 'bg-red-50 text-red-600'}`}>
          <Icon className="w-5 h-5" />
        </div>
        
        <p className="text-sm font-semibold text-gray-500 pt-0.5 tracking-wide">
          {title}
        </p>
      </div>

      <div className="flex items-end gap-1.5 mb-2">
        <h2 className="text-3xl font-bold text-gray-900 tracking-tight">
          {value.toLocaleString()} 
        </h2>
        {unit && (
          <span className="text-sm font-medium text-gray-400 pb-1">
            {unit}
          </span>
        )}
      </div>

      <p className="text-xs text-orange-500 font-semibold bg-orange-50 inline-block px-2 py-0.5 rounded-md">
        {change}
      </p>
    </div>
  );
};

export default StatCard;