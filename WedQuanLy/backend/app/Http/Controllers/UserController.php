<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class UserController extends Controller
{
    private $projectId = 'appdinhhuong'; // ID Firebase của Toàn

    public function index(Request $request)
    {
        $search = $request->query('search');
        
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/NguoiDung";
        
        $response = Http::get($url);

        if ($response->failed()) {
            return response()->json(['status' => 'error', 'message' => 'Không thể kết nối Firestore'], 500);
        }

        $data = $response->json();
        $users = [];

        if (isset($data['documents'])) {
            foreach ($data['documents'] as $doc) {
                $fields = $doc['fields'];
                $pathParts = explode('/', $doc['name']);
                $id = end($pathParts);

                $user = [
                    'id' => $id,
                    'Ho' => $fields['Ho']['stringValue'] ?? '',
                    'Ten' => $fields['Ten']['stringValue'] ?? '',
                    'Email' => $fields['Email']['stringValue'] ?? '',
                    'TrangThai' => $fields['TrangThai']['stringValue'] ?? 'active',
                    'VaiTro' => $fields['VaiTro']['stringValue'] ?? '',
                ];

                if ($search) {
                    if (str_contains(strtolower($user['Ten']), strtolower($search)) || 
                        str_contains(strtolower($user['Email']), strtolower($search))) {
                        $users[] = $user;
                    }
                } else {
                    $users[] = $user;
                }
            }
        }

        return response()->json([
            'status' => 'success',
            'data' => $users
        ]);
    }

    public function toggleStatus(Request $request, $id)
{
    $urlGet = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/NguoiDung/{$id}";
    $userResponse = Http::get($urlGet);

    if ($userResponse->failed()) {
        return response()->json(['success' => false, 'message' => 'User không tồn tại'], 404);
    }

    $userData = $userResponse->json();
    $currentStatus = $userData['fields']['TrangThai']['stringValue'] ?? 'active';

    $newStatus = ($currentStatus === 'active') ? 'locked' : 'active';

    $urlPatch = "{$urlGet}?updateMask.fieldPaths=TrangThai";
    $patchResponse = Http::patch($urlPatch, [
        'fields' => [
            'TrangThai' => ['stringValue' => $newStatus]
        ]
    ]);

    return response()->json([
        'success' => $patchResponse->successful(),
        'newStatus' => $newStatus
    ]);
}

    public function changeStatus(Request $request)
    {
        $id = $request->id;
        $newStatus = $request->newStatus;
        
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/NguoiDung/{$id}?updateMask.fieldPaths=TrangThai";

        $response = Http::patch($url, [
            'fields' => [
                'TrangThai' => ['stringValue' => $newStatus]
            ]
        ]);

        return response()->json(['success' => $response->successful()]);
    }
}