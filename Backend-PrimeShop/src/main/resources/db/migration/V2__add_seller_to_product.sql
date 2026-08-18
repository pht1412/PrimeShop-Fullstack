ALTER TABLE product ADD seller_id BIGINT;
ALTER TABLE product ADD CONSTRAINT FK_product_seller FOREIGN KEY (seller_id) REFERENCES seller_profile(id);

ALTER TABLE product ADD status VARCHAR(20) DEFAULT 'PENDING';