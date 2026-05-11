<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class QuestionController extends Controller
{
    private $projectId = 'appdinhhuong';

    /**
     * Lấy danh sách câu hỏi từ Firestore và format lại cho React
     */
    public function show($testId)
    {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/BaiTest/{$testId}";
        $response = Http::get($url);

        if ($response->successful()) {
            $data = $response->json();
            $questions = $data['fields']['DanhSachCauHoi']['arrayValue']['values'] ?? [];

            $formattedQuestions = array_map(function ($item) {
                $q = $item['mapValue']['fields'];
                return [
                    'id' => $q['MaCauHoi']['stringValue'] ?? '',
                    'content' => $q['NoiDung']['stringValue'] ?? '',
                    'order' => (int)($q['ThuTu']['integerValue'] ?? 0),
                   'answers' => array_map(function ($ans) {
    $a = $ans['mapValue']['fields'];
    return [
        'text' => $a['NoiDung']['stringValue'] ?? ($a['text']['stringValue'] ?? ''), 
        'point' => (int)($a['GiaTri']['integerValue'] ?? ($a['point']['integerValue'] ?? 0)),
        'maThangDo' => $a['MaThangDo']['stringValue'] ?? ($a['maThangDo']['stringValue'] ?? ''),
        'maDapAn' => $a['MaDapAn']['stringValue'] ?? ($a['maDapAn']['stringValue'] ?? '')
    ];
}, $q['DapAn']['arrayValue']['values'] ?? [])
                ];
            }, $questions);

            return response()->json(['status' => 'success', 'data' => $formattedQuestions]);
        }
        return response()->json(['status' => 'error', 'message' => 'Không tìm thấy bài test'], 404);
    }


    public function updateQuestions(Request $request, $testId)
    {
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/BaiTest/{$testId}?updateMask.fieldPaths=DanhSachCauHoi";

        $formattedQuestions = [];

        foreach ($request->questions as $q) {
            $answers = [];

            // Duyệt qua từng đáp án trong câu hỏi $q gửi từ React
            if (isset($q['answers']) && is_array($q['answers'])) {
                foreach ($q['answers'] as $ans) {
                    $answers[] = ['mapValue' => ['fields' => [
                        'NoiDung' => ['stringValue' => $ans['text'] ?? ''], // Lưu 'text' từ React vào 'NoiDung' Firebase
                        'GiaTri' => ['integerValue' => (int)($ans['point'] ?? 0)],
                        'MaThangDo' => ['stringValue' => $ans['maThangDo'] ?? ''],
                        'MaDapAn' => ['stringValue' => $ans['maDapAn'] ?? ($q['id'] . '-' . uniqid())]
                    ]]];
                }
            }

            // Đóng gói từng câu hỏi theo chuẩn Firestore Map
            $formattedQuestions[] = ['mapValue' => ['fields' => [
                'MaCauHoi' => ['stringValue' => $q['id']],
                'NoiDung' => ['stringValue' => $q['content'] ?? ''],
                'ThuTu' => ['integerValue' => (int)($q['order'] ?? 0)],
                'DapAn' => ['arrayValue' => ['values' => $answers]]
            ]]];
        }

        $payload = [
            'fields' => [
                'DanhSachCauHoi' => [
                    'arrayValue' => [
                        'values' => $formattedQuestions
                    ]
                ]
            ]
        ];

        $response = Http::patch($url, $payload);

        if ($response->successful()) {
            return response()->json(['status' => 'success', 'message' => 'Cập nhật cấu trúc câu hỏi thành công']);
        }

        return response()->json([
            'status' => 'error',
            'message' => 'Lỗi cập nhật Firestore',
            'details' => $response->json()
        ], 500);
    }
}