<?php
require_once __DIR__ . '/../vendor/autoload.php';
use Google\Cloud\Firestore\FirestoreClient;

class FirebaseConfig {
    private static $instance = null;

    public static function getConnection() {
        if (self::$instance === null) {
            self::$instance = new FirestoreClient([
                'keyFilePath' => __DIR__ . '/firebase-key.json',
                'projectId' => 'appdinhhuong'
            ]);
        }
        return self::$instance;
    }
}