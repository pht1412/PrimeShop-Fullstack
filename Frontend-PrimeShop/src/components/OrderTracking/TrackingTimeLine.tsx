import React from "react";
import { OrderStep } from "../../services/order";
import styles from "./styles/OrderTrackingPages.module.css"; // Import CSS Modules

interface TrackingTimelineProps {
  steps: OrderStep[];
}

const TrackingTimeLine: React.FC<TrackingTimelineProps> = ({ steps }) => {
  return (
    <div className={styles.timeline}>
      {steps.map((step, index) => (
        <div
          key={step.id}
          className={`${styles.timelineStep} ${
            step.completed ? styles.completed : ""
          }`}
        >
          <div className={styles.timelineDot}></div>
          <div className={styles.timelineInfo}>
            <h3>{step.title}</h3>
            <p>{step.date || "Chưa hoàn thành"}</p>
          </div>
        </div>
      ))}
    </div>
  );
};

export default TrackingTimeLine;