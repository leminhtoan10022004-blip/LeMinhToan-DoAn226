<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class CategoriesController extends Controller
{
    private $projectId = 'appdinhhuong'; // Project ID của Toàn

    public function index()
    {
        // Truy vấn đến collection 'NganhNghe' hoặc 'Categories' trong Firestore của bạn
        $url = "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/Nganh";
        
        $response = Http::get($url);

        if ($response->failed()) {
            return response()->json(['status' => 'error', 'message' => 'Không thể kết nối Firestore'], 500);
        }

        $data = $response->json();
        $categories = [];

        if (isset($data['documents'])) {
            foreach ($data['documents'] as $doc) {
                $fields = $doc['fields'] ?? [];
                $id = collect(explode('/', $doc['name']))->last();

                $categories[] = [
                    'id' => $id,
                    'title' => $fields['TenNganh']['stringValue'] ?? ($fields['name']['stringValue'] ?? 'N/A'),
                ];
            }
        }

        return response()->json([
            'status' => 'success',
            'data' => $categories
        ]);
    }
}
