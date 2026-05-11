<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class TestController extends Controller
{
    private $projectId = 'appdinhhuong';

    public function index()
    {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/BaiTest";
        $response = Http::get($url);

        $tests = [];
        if ($response->successful() && isset($response->json()['documents'])) {
            foreach ($response->json()['documents'] as $doc) {
                $fields = $doc['fields'];
                $pathParts = explode('/', $doc['name']);
                $tests[] = [
                    'id' => end($pathParts),
                    'TieuDe' => $fields['TieuDe']['stringValue'] ?? '',
                    'LoaiTest' => $fields['LoaiTest']['stringValue'] ?? '',
                    'ThoiGian' => $fields['ThoiGian']['integerValue'] ?? 0,
                    'SoLuongCauHoi' => $fields['SoLuongCauHoi']['integerValue'] ?? 0,
                    'TrangThai' => $fields['TrangThai']['stringValue'] ?? 'active',
                ];
            }
        }
        return response()->json(['status' => 'success', 'data' => $tests]);
    }

    public function destroy($id)
    {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/BaiTest/{$id}";
        $response = Http::delete($url);
        return response()->json(['success' => $response->successful()]);
    }

    public function store(Request $request)
    {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/BaiTest";
        $payload = [
            'fields' => [
                'TieuDe' => ['stringValue' => $request->TieuDe],
                'LoaiTest' => ['stringValue' => $request->LoaiTest],
                'ThoiGian' => ['integerValue' => (int)$request->ThoiGian],
                'SoLuongCauHoi' => ['integerValue' => 0],
                'TrangThai' => ['stringValue' => 'active']
            ]
        ];
        $response = Http::post($url, $payload);
        return response()->json(['success' => $response->successful()]);
    }


    public function getQuestions($id)
    {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/BaiTest/{$id}";
        $response = Http::get($url);
    
        if ($response->failed()) {
            return response()->json(['status' => 'error', 'message' => 'Không tìm thấy bài test'], 404);
        }
    
        $data = $response->json();
        $questions = [];

        $rawQuestions = $data['fields']['DanhSachCauHoi']['arrayValue']['values'] ?? [];
    
        foreach ($rawQuestions as $item) {
            $qFields = $item['mapValue']['fields'] ?? [];
            
            $answers = [];
            if (isset($qFields['DapAn']['arrayValue']['values'])) {
                foreach ($qFields['DapAn']['arrayValue']['values'] as $ans) {
                    $aFields = $ans['mapValue']['fields'] ?? [];
                    $answers[] = [
                        'maDapAn' => $aFields['MaDapAn']['stringValue'] ?? '', 
                        'text' => $aFields['NoiDung']['stringValue'] ?? '',
                        'point' => (int)($aFields['GiaTri']['integerValue'] ?? 0),
                        'maThangDo' => $aFields['MaThangDo']['stringValue'] ?? ''
                    ];
                }
            }
    
            $questions[] = [
                'id' => $qFields['MaCauHoi']['stringValue'] ?? uniqid(),
                'content' => $qFields['NoiDung']['stringValue'] ?? '',
                'order' => (int)($qFields['ThuTu']['integerValue'] ?? 0),
                'answers' => $answers
            ];
        }

        usort($questions, fn($a, $b) => $a['order'] <=> $b['order']);
    
        return response()->json([
            'status' => 'success',
            'data' => $questions
        ]);
    }
}