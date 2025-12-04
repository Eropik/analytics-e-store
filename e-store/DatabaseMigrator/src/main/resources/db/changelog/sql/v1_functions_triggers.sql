-- ===================================
-- 6. ТРИГГЕРЫ
-- ===================================
CREATE OR REPLACE FUNCTION update_customer_spent()
RETURNS TRIGGER AS $$
DECLARE
delivered_id INT;
BEGIN
SELECT status_id INTO delivered_id FROM order_status WHERE status_name = 'DELIVERED';

IF NEW.status_id = delivered_id AND (OLD.status_id IS DISTINCT FROM delivered_id) THEN
UPDATE customer_profile
SET total_spent = total_spent + NEW.total_amount,
    orders_count = orders_count + 1
WHERE customer_profile.user_id = NEW.user_id;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
//  -- Конец первой команды

CREATE TRIGGER trg_update_customer_spent
    AFTER UPDATE OF status_id ON "order"
    FOR EACH ROW
    EXECUTE FUNCTION update_customer_spent();
// -- Конец второй команды