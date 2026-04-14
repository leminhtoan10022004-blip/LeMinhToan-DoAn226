<?php

namespace App\Models; 

use Illuminate\Foundation\Auth\User as Authenticatable;

class UserModel extends Authenticatable 
{
    protected $table = 'NguoiDung';
    public $timestamps = false;
    protected $fillable = ['Ho', 'Ten', 'Email', 'MatKhau', 'VaiTro', 'TrangThai', 'SDT'];
}