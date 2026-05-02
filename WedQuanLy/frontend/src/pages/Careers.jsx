import React, { useState, useEffect } from 'react';
import api from '../api/axios'; 
import { PlusIcon, PencilSquareIcon, TrashIcon, XMarkIcon, BriefcaseIcon } from '@heroicons/react/24/outline';

const Careers = () => {
  const [jobs, setJobs] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingJob, setEditingJob] = useState(null);

  const [formData, setFormData] = useState({ title: '', description: '', salary: '', category_id: '' });

  const fetchData = async () => {
    try {
      setLoading(true);
      const [jobRes, catRes] = await Promise.all([api.get('/careers'), api.get('/categories')]);
      if (jobRes.data.status === 'success') setJobs(jobRes.data.data);
      if (catRes.data.status === 'success') setCategories(catRes.data.data);
    } catch (error) { console.error("Lỗi:", error); } 
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      if (editingJob) await api.put(`/careers/${editingJob.id}`, formData);
      else await api.post('/careers', formData);
      setIsModalOpen(false);
      fetchData();
    } catch (error) { alert("Lỗi lưu dữ liệu!"); }
    finally { setIsSaving(false); }
  };

  return (
    <div className="flex-1 p-8 bg-[#F8FAFC] min-h-screen">
      {/* Header */}
      <div className="flex justify-between items-center mb-10">
        <h1 className="text-3xl font-black text-slate-900 flex items-center gap-3">
          <BriefcaseIcon className="w-10 h-10 text-orange-500" /> Quản lý Công việc
        </h1>
        <button onClick={() => { setEditingJob(null); setFormData({title:'', description:'', salary:'', category_id:''}); setIsModalOpen(true); }}
          className="px-8 py-4 bg-orange-500 text-white rounded-2xl font-bold shadow-lg shadow-orange-200 hover:bg-orange-600 transition-all">
          + Tạo công việc
        </button>
      </div>

      {/* Table với Loading State */}
      <div className="bg-white rounded-[32px] shadow-sm border border-slate-100 overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-slate-50/50">
            <tr className="text-slate-400 text-[11px] uppercase tracking-widest font-black">
              <th className="pl-10 py-6">Mã ID</th>
              <th className="px-6 py-6">Thông tin</th>
              <th className="px-6 py-6 text-center">Mức lương</th>
              <th className="pr-10 py-6 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="4" className="py-20 text-center font-bold text-slate-400 animate-pulse">Đang tải dữ liệu...</td></tr>
            ) : jobs.map((job) => (
              <tr key={job.id} className="border-t border-slate-50 group hover:bg-slate-50">
                <td className="pl-10 py-6 font-mono text-xs text-slate-400">#{job.id.substring(0,8)}</td>
                <td className="px-6 py-6">
                  <p className="font-bold text-slate-800">{job.title}</p>
                  <p className="text-slate-400 text-xs line-clamp-1">{job.description}</p>
                </td>
                <td className="px-6 py-6 text-center">
                  <span className="px-3 py-1 bg-green-50 text-green-600 rounded-lg font-bold text-xs">{job.salary}</span>
                </td>
                <td className="pr-10 py-6 text-right">
                  <button onClick={() => { setEditingJob(job); setFormData(job); setIsModalOpen(true); }} className="p-2 text-slate-400 hover:text-orange-500"><PencilSquareIcon className="w-5 h-5"/></button>
                  <button onClick={async () => { if(window.confirm("Xóa?")) { await api.delete(`/careers/${job.id}`); fetchData(); } }} className="p-2 text-slate-400 hover:text-red-500"><TrashIcon className="w-5 h-5"/></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-md flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-[40px] p-10 w-full max-w-xl shadow-2xl relative">
            <h2 className="text-2xl font-black mb-8">{editingJob ? 'Sửa công việc' : 'Thêm công việc'}</h2>
            <form onSubmit={handleSave} className="space-y-5">
              <input required value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} placeholder="Tên công việc" className="w-full p-4 bg-slate-50 rounded-2xl outline-none focus:ring-2 focus:ring-orange-500/20" />
              
              <select required value={formData.category_id} onChange={e => setFormData({...formData, category_id: e.target.value})} className="w-full p-4 bg-slate-50 rounded-2xl outline-none">
                <option value="">-- Chọn ngành nghề --</option>
                {categories.map(cat => <option key={cat.id} value={cat.id}>{cat.title}</option>)}
              </select>

              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 bg-slate-100 rounded-2xl text-slate-400 text-xs font-mono">{editingJob ? editingJob.id : 'ID TỰ ĐỘNG'}</div>
                <input value={formData.salary} onChange={e => setFormData({...formData, salary: e.target.value})} placeholder="Mức lương" className="p-4 bg-slate-50 rounded-2xl outline-none" />
              </div>

              <textarea value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} placeholder="Mô tả ngắn" className="w-full p-4 bg-slate-50 rounded-2xl h-32 outline-none" />

              <button disabled={isSaving} className={`w-full py-5 rounded-3xl font-black text-white transition-all ${isSaving ? 'bg-orange-300' : 'bg-orange-500 hover:bg-orange-600'}`}>
                {isSaving ? 'ĐANG LƯU...' : (editingJob ? 'CẬP NHẬT' : 'TẠO CÔNG VIỆC')}
              </button>
            </form>
            <button onClick={() => setIsModalOpen(false)} className="absolute top-8 right-8 text-slate-300"><XMarkIcon className="w-8 h-8" /></button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Careers;