import React, { useEffect, useState } from "react";
import api from "../../api/api";
import { Button, Alert, Radio, RadioGroup, FormControlLabel, CircularProgress } from "@mui/material";
import { useParams, useNavigate } from "react-router-dom";
import Swal from "sweetalert2";
import "./games.css";

interface Question {
  question: string;
  options: string[];
}

const MinigamePage: React.FC = () => {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);
  
  // State quản lý kết quả
  const [result, setResult] = useState<{ success: boolean; message?: string; voucher?: any } | null>(null);
  
  // State riêng cho từng game
  const [quizAnswers, setQuizAnswers] = useState<number[]>([]);
  const [isSpinning, setIsSpinning] = useState(false);
  const [rotation, setRotation] = useState(0);
  const [diceValues, setDiceValues] = useState([1, 1, 1]);
  const [isRolling, setIsRolling] = useState(false);
  const [taiXiuChoice, setTaiXiuChoice] = useState<number | null>(null);
  const [cards, setCards] = useState<any[]>([]);
  const [flippedIndices, setFlippedIndices] = useState<number[]>([]);
  const [matchedPairs, setMatchedPairs] = useState<string[]>([]);

  // 1. Load dữ liệu
  useEffect(() => {
    if (!gameId) return;
    window.scrollTo(0, 0);
    
    const token = localStorage.getItem('token');
    if (!token) {
        Swal.fire({ 
            icon: 'warning', 
            title: 'Chưa đăng nhập', 
            text: 'Đăng nhập đi rồi hãy mơ đến kho báu!', 
            confirmButtonText: 'Đăng nhập ngay' 
        }).then(() => navigate('/login'));
        return;
    }

    api.get(`/minigame/questions?gameId=${gameId}`)
      .then(res => {
        const qList = res.data.questions || [];
        setQuestions(qList);
        
        // Init state game
        if (gameId === 'who_wants_to_be_millionaire') {
            setQuizAnswers(Array(qList.length).fill(-1));
        } else if (gameId === 'memory_card' && qList.length > 0) {
            const items = qList[0].options;
            const deck = [...items, ...items]
                .sort(() => Math.random() - 0.5)
                .map((item, idx) => ({ id: idx, content: item, isFlipped: false, isMatched: false }));
            setCards(deck);
        }
      })
      .catch(err => {
          console.error(err);
          Swal.fire("Lỗi", "Không tải được dữ liệu game.", "error").then(() => navigate('/minigame-list'));
      })
      .finally(() => setLoading(false));
  }, [gameId, navigate]);

  // 2. Xử lý nhận thưởng chung
  const handleClaimReward = async (customAnswers: number[] = []) => {
      try {
        const userData = localStorage.getItem("user");
        const userId = userData ? JSON.parse(userData).id : 1;
        const res = await api.post(`/minigame/play?gameId=${gameId}`, { answers: customAnswers, userId });
        
        // --- QUAN TRỌNG: CẬP NHẬT GIAO DIỆN TỪ KẾT QUẢ SERVER ---
        
        // Nếu là Tài Xỉu, cập nhật lại xúc xắc theo kết quả thật từ server trả về
        if (gameId === 'tai_xiu' && res.data.dice) {
            setDiceValues(res.data.dice);
        }

        setResult(res.data);
      } catch (err) {
        setResult({ success: false, message: "Lỗi kết nối!" });
      } finally {
          setIsSpinning(false); // Dừng quay
      }
  };

  // --- GAME LOGIC: LUCKY WHEEL ---
  const spinWheel = () => {
      if (isSpinning || result?.success) return;
      setIsSpinning(true);
      // Quay ít nhất 5 vòng (1800 độ) + random
      const newRotation = rotation + 1800 + Math.floor(Math.random() * 360);
      setRotation(newRotation);

      setTimeout(() => {
          handleClaimReward([0]); // Gửi kết quả lên server
      }, 4000);
  };

  // --- GAME LOGIC: TAI XIU ---
  const rollDice = () => {
      if (isRolling || taiXiuChoice === null || result?.success) return;
      setIsRolling(true);
      
      const interval = setInterval(() => {
          setDiceValues([
              Math.floor(Math.random() * 6) + 1,
              Math.floor(Math.random() * 6) + 1,
              Math.floor(Math.random() * 6) + 1
          ]);
      }, 100);

      setTimeout(() => {
          clearInterval(interval);
          setIsRolling(false);
          // Gọi API để lấy kết quả thật và cập nhật lại diceValues ở handleClaimReward
          handleClaimReward([taiXiuChoice]);
      }, 2000);
  };

  // --- GAME LOGIC: MEMORY CARD ---
  const handleCardClick = (idx: number) => {
      if (flippedIndices.length >= 2 || cards[idx].isFlipped || cards[idx].isMatched) return;
      
      const newCards = [...cards];
      newCards[idx].isFlipped = true;
      setCards(newCards);
      
      const newFlipped = [...flippedIndices, idx];
      setFlippedIndices(newFlipped);

      if (newFlipped.length === 2) {
          const [idx1, idx2] = newFlipped;
          if (newCards[idx1].content === newCards[idx2].content) {
              newCards[idx1].isMatched = true;
              newCards[idx2].isMatched = true;
              setMatchedPairs(prev => [...prev, newCards[idx1].content]);
              setFlippedIndices([]);
              
              if (matchedPairs.length + 1 === questions[0].options.length) {
                  setTimeout(() => handleClaimReward([0]), 500);
              }
          } else {
              setTimeout(() => {
                  newCards[idx1].isFlipped = false;
                  newCards[idx2].isFlipped = false;
                  setCards([...newCards]);
                  setFlippedIndices([]);
              }, 1000);
          }
      }
  };

  // 3. UI Renderer
  const renderGameContent = () => {
      if (!questions.length) return <div>Đang tải bàn chơi...</div>;

      // === LUCKY WHEEL ===
      if (gameId === 'lucky_wheel') {
          const segments = questions[0].options;
          const segAngle = 360 / segments.length;
          return (
              <div className="game-center-wrapper">
                  <div className="wheel-container">
                      <div className="wheel-pointer">🔻</div>
                      <div className="wheel" style={{ transform: `rotate(${rotation}deg)`, transition: 'transform 4s cubic-bezier(0.25, 0.1, 0.25, 1)' }}>
                          {segments.map((seg, i) => (
                              <div key={i} className="wheel-segment" style={{ transform: `rotate(${i * segAngle}deg) skewY(-${90 - segAngle}deg)` }}>
                                  <span className="wheel-text" style={{ transform: `skewY(${90 - segAngle}deg) rotate(${segAngle / 2}deg)` }}>{seg}</span>
                              </div>
                          ))}
                      </div>
                  </div>
                  {/* NÚT QUAY ĐÃ RA KHỎI CONTAINER ĐỂ HIỂN THỊ ĐÚNG */}
                  <Button 
                    variant="contained" 
                    className="spin-btn-action" 
                    onClick={spinWheel} 
                    disabled={isSpinning || !!result?.success}
                  >
                      {isSpinning ? "ĐANG QUAY..." : "QUAY NGAY 🎲"}
                  </Button>
              </div>
          );
      }

      // === TAI XIU ===
      if (gameId === 'tai_xiu') {
          return (
              <div className="taixiu-container">
                  <div className={`dice-box ${isRolling ? 'shaking' : ''}`}>
                      {diceValues.map((val, i) => (
                          <div key={i} className={`dice dice-${val}`}>{val}</div>
                      ))}
                  </div>
                  <div className="taixiu-options">
                      <div className={`bet-box ${taiXiuChoice === 0 ? 'selected' : ''}`} onClick={() => setTaiXiuChoice(0)}>TÀI (11-17)</div>
                      <div className={`bet-box ${taiXiuChoice === 1 ? 'selected' : ''}`} onClick={() => setTaiXiuChoice(1)}>XỈU (4-10)</div>
                  </div>
                  <Button variant="contained" className="game-btn" onClick={rollDice} disabled={isRolling || taiXiuChoice === null || !!result?.success}>
                      {isRolling ? "Đang lắc..." : "MỞ BÁT"}
                  </Button>
              </div>
          );
      }

      // === MEMORY CARD ===
      if (gameId === 'memory_card') {
          return (
              <div className="memory-grid">
                  {cards.map((card, idx) => (
                      <div key={idx} className={`memory-card-flip ${card.isFlipped || card.isMatched ? 'flipped' : ''}`} onClick={() => handleCardClick(idx)}>
                          <div className="card-front">?</div>
                          <div className="card-back">{card.content}</div>
                      </div>
                  ))}
              </div>
          );
      }

      // === DEFAULT QUIZ ===
      return (
          <div className="quiz-container">
              {questions.map((q, idx) => (
                  <div className="minigame-question-card" key={idx}>
                      <div className="minigame-question-title">Câu {idx + 1}: {q.question}</div>
                      <RadioGroup
                          value={quizAnswers[idx]}
                          onChange={e => {
                              const newAns = [...quizAnswers];
                              newAns[idx] = Number(e.target.value);
                              setQuizAnswers(newAns);
                          }}
                      >
                          {q.options.map((opt, oidx) => (
                              <FormControlLabel key={oidx} value={oidx} control={<Radio />} label={opt} disabled={!!result?.success} />
                          ))}
                      </RadioGroup>
                  </div>
              ))}
              <Button className="minigame-submit-btn" variant="contained" onClick={() => handleClaimReward(quizAnswers)} disabled={quizAnswers.includes(-1) || !!result?.success}>
                  Trả lời
              </Button>
          </div>
      );
  };

  // 4. POPUP CHIẾN THẮNG "PHONG THÁI NHÀ VUA"
  useEffect(() => {
    if (result && result.success && result.voucher) {
      // Bắn pháo hoa (backdrop gif) + Giao diện Gold/Royal
      Swal.fire({
        title: '<span class="king-title">👑 CHIẾN THẮNG VINH QUANG! 👑</span>',
        html: `
            <div class="king-content">
                <p class="king-message">Chúc mừng Nhà Vua đã chinh phục thử thách!</p>
                <div class="king-reward-box">
                    <div class="reward-label">Phần thưởng của Ngài</div>
                    <div class="reward-value">${parseInt(result.voucher.discountValue).toLocaleString()} VNĐ</div>
                    <div class="reward-code">
                        CODE: ${result.voucher.code}
                    </div>
                    <div class="reward-expiry">HSD: ${result.voucher.endDate?.slice(0, 10)}</div>
                </div>
            </div>
        `,
        width: 600,
        padding: '2em',
        background: '#fff url(https://i.gifer.com/origin/23/2338330196101446304704734143155.gif) center center no-repeat', // Ảnh nền pháo hoa
        backdrop: `
            rgba(0,0,123,0.4)
            url("https://i.gifer.com/origin/23/2338330196101446304704734143155.gif")
            left top
            no-repeat
        `,
        showConfirmButton: true,
        confirmButtonText: '🏆 NHẬN THƯỞNG NGAY 🏆',
        buttonsStyling: false,
        customClass: {
            popup: 'king-popup-container',
            confirmButton: 'king-confirm-btn',
            title: 'king-swal-title',
            htmlContainer: 'king-swal-html'
        }
      });
    } else if (result && !result.success) {
        Swal.fire({
            icon: 'error',
            title: 'Chưa may mắn rồi!',
            text: result.message || 'Hãy thử lại nhé!',
            confirmButtonColor: '#3b82f6',
            customClass: {
                popup: 'loser-popup',
                confirmButton: 'loser-confirm-btn'
            }
        });
    }
  }, [result]);

  if (loading) return <div style={{textAlign:'center', padding:50}}><CircularProgress /></div>;

  return (
    <div className="minigame-container">
      <div className="minigame-title">
          {gameId === 'lucky_wheel' && "🎡 Vòng Quay May Mắn"}
          {gameId === 'tai_xiu' && "🎲 Tài Xỉu Đại Chiến"}
          {gameId === 'memory_card' && "🎴 Thử Tài Trí Nhớ"}
          {gameId === 'who_wants_to_be_millionaire' && "💡 Ai Là Triệu Phú"}
      </div>

      {renderGameContent()}
    </div>
  );
};

export default MinigamePage;