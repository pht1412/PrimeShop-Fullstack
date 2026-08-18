import React, { useEffect, useState } from "react";
import api from "../../api/api";
import { Button, CircularProgress } from "@mui/material";
import { useNavigate } from "react-router-dom";
import "./games.css"; // Import CSS cùng thư mục

const GameListPage: React.FC = () => {
  const [games, setGames] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    // Scroll lên đầu trang khi vào
    window.scrollTo(0, 0);

    api.get("/minigame/list")
      .then(res => {
          // Backend trả về Map.of("games", list) nên truy cập res.data.games
          setGames(res.data.games || []);
      })
      .catch(err => {
          console.error("Lỗi tải danh sách game:", err);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
      return (
          <div className="games-container" style={{display: 'flex', justifyContent: 'center', minHeight: '300px', alignItems: 'center'}}>
              <CircularProgress />
          </div>
      );
  }

  return (
    <div className="games-container">
      <div className="games-title">🎮 Sân Chơi Săn Voucher 🎁</div>
      
      {games.length === 0 ? (
          <div style={{textAlign: 'center', color: '#64748b'}}>Hiện chưa có trò chơi nào. Quay lại sau nhé!</div>
      ) : (
          <div className="games-list-grid">
            {games.map(game => (
              <div className="game-card" key={game.gameId}>
                <div className="game-card-icon">{game.icon}</div>
                <div className="game-card-title">{game.name}</div>
                <div className="game-card-desc">{game.description}</div>
                <Button
                  variant="contained"
                  onClick={() => navigate(`/minigame/${game.gameId}`)}
                >
                  Chơi ngay
                </Button>
              </div>
            ))}
          </div>
      )}
    </div>
  );
};

export default GameListPage;