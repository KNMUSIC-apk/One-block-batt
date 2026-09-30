package com.oneblock.core.api;

public enum GameState {
    IDLE,        // Không có gì
    WAITING,     // Chờ người chơi
    COUNTDOWN,   // Đang đếm ngược 3 phút
    STARTING,    // Đang đếm ngược 3 giây thả
    RUNNING,     // Trận đang chạy
    ENDING,      // Đang kết thúc / trao thưởng
    RESETTING    // Đang reset map
}
