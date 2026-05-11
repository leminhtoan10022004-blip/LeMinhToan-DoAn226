<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\UserController;
use App\Http\Controllers\CareerController;
use App\Http\Controllers\CategoriesController;
use App\Http\Controllers\TestController;
use App\Http\Controllers\QuestionController;


Route::middleware('auth:sanctum')->get('/user', function (Request $request) {
    return $request->user();
});

Route::prefix('users')->group(function () {
    Route::get('/', [UserController::class, 'index']);
    Route::post('/toggle-status/{id}', [UserController::class, 'toggleStatus']);
    Route::post('/update-status', [UserController::class, 'changeStatus']);
});

Route::get('/careers', [CareerController::class, 'index']);
Route::get('/categories', [CategoriesController::class, 'index']);
Route::post('/careers', [CareerController::class, 'store']);
Route::post('/categories', [CategoriesController::class, 'store']);
Route::delete('/careers/{id}', [CareerController::class, 'destroy']);
Route::put('/careers/{id}', [CareerController::class, 'update']);

Route::prefix('tests')->group(function () {
    Route::get('/', [TestController::class, 'index']);
    Route::post('/', [TestController::class, 'store']);
    Route::delete('/{id}', [TestController::class, 'destroy']);
    Route::get('/{id}/questions', [TestController::class, 'getQuestions']);
    Route::put('/{id}/questions', [QuestionController::class, 'updateQuestions']);
});

Route::get('/{id}/questions', [QuestionController::class, 'show']);