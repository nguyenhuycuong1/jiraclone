ALTER TABLE projects ENABLE ROW LEVEL SECURITY;

CREATE POLICY org_isolation ON projects
       USING (org_id = current_setting('app.current_org_id')::"uuid");

-- 11:05 8/6/2026 cuongnh

-- Xóa policy cũ
DROP POLICY org_isolation ON projects;

-- Tạo lại với missing_ok = true
CREATE POLICY org_isolation ON projects
    USING (
    org_id = nullif(
            current_setting('app.current_org_id', true), -- true = missing_ok, trả về NULL thay vì lỗi
            ''  -- nếu là chuỗi rỗng thì cũng thành NULL
             )::uuid
    );
