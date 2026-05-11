import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { 
  ChevronDownIcon, PlusIcon, TrashIcon, 
  ArrowLeftIcon, CloudArrowUpIcon
} from '@heroicons/react/24/outline';
import { toast } from 'sonner';

const QuestionManagement = () => {
  const { testId } = useParams();
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);
  const [isSaving, setIsSaving] = useState(false);

  const fetchQuestions = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/tests/${testId}/questions`);
      if (response.data.status === 'success') {
        setQuestions(response.data.data);
      }
    } catch (error) {
      toast.error("Lỗi khi tải câu hỏi");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (testId) fetchQuestions();
  }, [testId]);

  // --- LOGIC ĐÁP ÁN ---
  const addAnswer = (qIdx) => {
    const newQuestions = [...questions];
    const newAns = { 
      text: '', 
      point: 1, 
      maThangDo: 'TD-HOLLAND-R', 
      maDapAn: `DA-${newQuestions[qIdx].id.split('-')[1]}-${Date.now()}` 
    };
    newQuestions[qIdx].answers.push(newAns);
    setQuestions(newQuestions);
  };

  const deleteAnswer = (qIdx, aIdx) => {
    const newQuestions = [...questions];
    newQuestions[qIdx].answers.splice(aIdx, 1);
    setQuestions([...newQuestions]);
  };

  const updateAnswerField = (qIdx, aIdx, field, value) => {
    const newQuestions = [...questions];
    newQuestions[qIdx].answers[aIdx][field] = field === 'point' ? parseInt(value) || 0 : value;
    setQuestions([...newQuestions]);
  };

  // --- LOGIC LƯU TỔNG ---
  const handleSaveAll = async () => {
    try {
      setIsSaving(true);
      const response = await api.put(`/tests/${testId}/questions`, { questions });
      if (response.data.status === 'success') {
        toast.success("Cập nhật thành công lên Firebase!");
      }
    } catch (error) {
      toast.error("Lỗi khi lưu dữ liệu");
    } finally {
      setIsSaving(false);
    }
  };

  if (loading) return <div className="p-10 text-center animate-pulse">Đang đồng bộ dữ liệu...</div>;

  return (
    <div className="flex-1 p-6 lg:p-10 bg-gray-50 min-h-screen font-sans">
      <div className="mb-8 flex justify-between items-end">
        <div>
          <button onClick={() => navigate('/tests')} className="flex items-center gap-2 text-sm text-gray-400 mb-2 hover:text-orange-500 transition-all font-medium">
            <ArrowLeftIcon className="w-4 h-4" /> Danh sách bài test
          </button>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">Cấu trúc: <span className="text-orange-500">{testId}</span></h1>
        </div>
        <button 
          onClick={handleSaveAll}
          disabled={isSaving}
          className={`flex items-center gap-2 px-6 py-3 ${isSaving ? 'bg-gray-300' : 'bg-green-600 hover:bg-green-700'} text-white rounded-2xl text-sm font-bold shadow-lg transition-all`}
        >
          <CloudArrowUpIcon className="w-5 h-5" /> {isSaving ? 'Đang lưu...' : 'Lưu vào Firebase'}
        </button>
      </div>

      <div className="space-y-4">
        {questions.map((q, qIdx) => (
          <div key={q.id || qIdx} className="bg-white rounded-[24px] border border-gray-100 shadow-sm overflow-hidden transition-all">
            <div 
              className={`p-6 flex items-center justify-between cursor-pointer ${expandedId === (q.id || qIdx) ? 'bg-orange-50/40' : 'hover:bg-gray-50'}`}
              onClick={() => setExpandedId(expandedId === (q.id || qIdx) ? null : (q.id || qIdx))}
            >
              <div className="flex items-center gap-5">
                <span className="w-10 h-10 flex items-center justify-center bg-white border border-gray-100 rounded-xl text-orange-600 font-bold shadow-sm">{q.order}</span>
                <p className="font-bold text-gray-700 text-lg">{q.content}</p>
              </div>
              <ChevronDownIcon className={`w-5 h-5 text-gray-400 transition-transform ${expandedId === (q.id || qIdx) ? 'rotate-180' : ''}`} />
            </div>

            {expandedId === (q.id || qIdx) && (
              <div className="p-8 bg-white border-t border-gray-50 animate-in fade-in slide-in-from-top-2">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {(q.answers || []).map((ans, aIdx) => (
                    <div key={aIdx} className="group relative flex flex-col p-5 rounded-[20px] border border-gray-100 bg-gray-50/30 hover:border-orange-200 hover:bg-white transition-all">
                      <button onClick={() => deleteAnswer(qIdx, aIdx)} className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-all shadow-md z-20">
                        <TrashIcon className="w-3 h-3" />
                      </button>

                      <div className="flex items-center justify-between mb-4">
                        <div className="flex items-center gap-2 bg-white px-2 py-1 rounded-lg border border-gray-100">
                          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-tighter">Điểm</span>
                          <input 
                            type="number" 
                            value={ans.point} 
                            onChange={(e) => updateAnswerField(qIdx, aIdx, 'point', e.target.value)}
                            className="w-8 text-[10px] font-black text-orange-600 outline-none" 
                          />
                        </div>
                        <select 
                          value={ans.maThangDo}
                          onChange={(e) => updateAnswerField(qIdx, aIdx, 'maThangDo', e.target.value)}
                          className="text-[10px] bg-transparent text-gray-500 font-bold outline-none border-none cursor-pointer"
                        >
                          <option value="TD-HOLLAND-R">Realistic</option>
                          <option value="TD-HOLLAND-I">Investigative</option>
                          <option value="TD-HOLLAND-A">Artistic</option>
                          <option value="TD-HOLLAND-S">Social</option>
                          <option value="TD-HOLLAND-E">Enterprising</option>
                          <option value="TD-HOLLAND-C">Conventional</option>
                        </select>
                      </div>

                      <textarea
                      value={ans.text || ""} // ans.text lúc này đã được Controller map từ NoiDung
                      rows="2"
                      onChange={(e) => updateAnswerField(qIdx, aIdx, "text", e.target.value)}
                      placeholder="Nhập nội dung đáp án..."
                      className="bg-transparent text-gray-700 font-semibold text-sm outline-none resize-none w-full"
                    />
                    </div>
                  ))}
                  <button onClick={() => addAnswer(qIdx)} className="flex flex-col items-center justify-center p-6 rounded-[20px] border-2 border-dashed border-gray-200 text-gray-300 hover:border-orange-300 hover:text-orange-500 transition-all group">
                    <PlusIcon className="w-8 h-8 mb-2 group-hover:scale-110 transition-transform" />
                    <span className="text-[10px] font-bold uppercase tracking-widest">Thêm đáp án</span>
                  </button>
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