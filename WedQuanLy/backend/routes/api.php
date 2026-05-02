<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\UserController;
use App\Http\Controllers\CareerController;
use App\Http\Controllers\CategoriesController;


/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| is assigned the "api" middleware group. Enjoy building your API!
|
*/

Route::middleware('auth:sanctum')->get('/user', function (Request $request) {
    return $request->user();
});

Route::get('/users', [UserController::class, 'index']);
Route::post('/users/toggle-status/{id}', [UserController::class, 'toggleStatus']);
Route::post('/users/update-status', [UserController::class, 'changeStatus']);
Route::get('/careers', [CareerController::class, 'index']);
Route::get('/categories', [CategoriesController::class, 'index']);
Route::post('/careers', [CareerController::class, 'store']);
Route::post('/categories', [CategoriesController::class, 'store']);
Route::delete('/careers/{id}', [CareerController::class, 'destroy']);
Route::put('/careers/{id}', [CareerController::class, 'update']);