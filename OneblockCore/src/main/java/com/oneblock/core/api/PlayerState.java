package com.oneblock.core.api;

public enum PlayerState {
    LOBBY,      // Ở ngoài, chưa tham gia
    QUEUE,      // Trong phòng chờ
    PLAYING,    // Đang thi đấu
    SPECTATOR,  // Đã bị loại, đang xem
    RESTORING   // Đang được khôi phục
}
