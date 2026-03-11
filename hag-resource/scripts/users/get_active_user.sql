SELECT
    u.id,
    u.email,
    u.status,
    u.last_login_at
FROM users u
WHERE u.email = '${username}'
  AND u.status = 'ACTIVE'
