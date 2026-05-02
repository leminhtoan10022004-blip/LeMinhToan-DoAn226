<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class CareerController extends Controller
{
    private $projectId = 'appdinhhuong'; // Project ID của bạn

    public function index(Request $request)
    {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/CongViec";
        $response = Http::get($url);
    
        if ($response->failed()) {
            return response()->json(['status' => 'error'], 500);
        }
    
        $data = $response->json();
        $jobs = [];
    
        if (isset($data['documents'])) {
            foreach ($data['documents'] as $doc) {
                $fields = $doc['fields'] ?? [];
                $pathParts = explode('/', $doc['name']);
                $jobId = end($pathParts); 
            
                $jobs[] = [
                    'id' => $jobId,
                    'title' => $fields['TenCongViec']['stringValue'] ?? 'N/A',
                    'code' => $fields['MaCV']['stringValue'] ?? $jobId, 
                    
                    'description' => $fields['MoTa']['stringValue'] ?? '',
                    'salary' => $fields['MucLuong']['stringValue'] ?? 'Thỏa thuận',
                ];
            }
        }
    
        return response()->json(['status' => 'success', 'data' => $jobs]);
    }

    public function store(Request $request) {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/CongViec";
        
        $payload = [
            'fields' => [
                'TenCongViec' => ['stringValue' => $request->title],
                // Bạn có thể lưu mã CV theo logic riêng hoặc bỏ qua nếu dùng ID tự động
                'MaCV' => ['stringValue' => $request->code ?? 'AUTO'], 
                'MoTa' => ['stringValue' => $request->description],
                'MucLuong' => ['stringValue' => $request->salary],
                'NganhId' => ['stringValue' => $request->category_id],
            ]
        ];
    
        // Gửi request POST không có documentId
        $response = Http::post($url, $payload);
        return response()->json(['status' => 'success', 'data' => $response->json()]);
    }

    public function update(Request $request, $id)
    {
        try {
            $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/CongViec/{$id}";
            $payload = [
                'fields' => [
                    'TenCongViec' => ['stringValue' => $request->title],
                    'MoTa' => ['stringValue' => $request->description],
                    'MucLuong' => ['stringValue' => $request->salary],
                    'NganhId' => ['stringValue' => $request->category_id],
                ]
            ];
            $response = Http::patch($url . "?updateMask.fieldPaths=TenCongViec&updateMask.fieldPaths=MoTa&updateMask.fieldPaths=MucLuong&updateMask.fieldPaths=NganhId", $payload);
    
            if ($response->successful()) {
                return response()->json(['status' => 'success', 'data' => $response->json()]);
            }
    
            return response()->json(['status' => 'error', 'message' => $response->body()], $response->status());
        } catch (\Exception $e) {
            return response()->json(['status' => 'error', 'message' => $e->getMessage()], 500);
        }
    }

    public function destroy($id) {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/CongViec/{$id}";
        $response = Http::delete($url);
        return response()->json(['success' => $response->successful()]);
    }
}