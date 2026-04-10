#!/bin/bash

# 宝塔Docker容器数据库自动备份脚本
# 功能：自动备份指定Docker容器中的MySQL/MariaDB数据库
# 使用方法：1. 修改脚本中的配置参数 2. 设置定时任务执行此脚本

# ===================================
# 配置参数
# ===================================

# Docker容器名称或ID
CONTAINER_NAME="tsdd-mysql-1"

# 数据库连接信息
DB_USER="root"
DB_PASSWORD="demo"
DB_NAME="test"

# 备份设置
BACKUP_DIR="/www/backup/database"
# 备份文件保留天数
RETENTION_DAYS=7

# 日志文件
LOG_FILE="/www/backup/logs/db_backup.log"

# ===================================
# 脚本开始
# ===================================

# 确保备份目录存在
mkdir -p "$BACKUP_DIR"
mkdir -p "$(dirname "$LOG_FILE")"

# 记录开始时间
START_TIME=$(date +"%Y-%m-%d %H:%M:%S")
echo "[$START_TIME] 开始备份数据库 $DB_NAME" >> "$LOG_FILE"

# 生成备份文件名
BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_$(date +"%Y%m%d_%H%M%S").sql.gz"

# 执行数据库备份（添加参数解决大数据包和锁定表问题）
docker exec "$CONTAINER_NAME" mysqldump --max_allowed_packet=1G --skip-lock-tables -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" | gzip > "$BACKUP_FILE"

# 检查备份是否成功
if [ $? -eq 0 ]; then
    END_TIME=$(date +"%Y-%m-%d %H:%M:%S")
    BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo "[$END_TIME] 备份成功！文件：$BACKUP_FILE，大小：$BACKUP_SIZE" >> "$LOG_FILE"
    
    # 清理过期备份
    find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +"$RETENTION_DAYS" -delete
    echo "[$END_TIME] 已清理 $RETENTION_DAYS 天前的过期备份" >> "$LOG_FILE"
else
    END_TIME=$(date +"%Y-%m-%d %H:%M:%S")
    echo "[$END_TIME] 备份失败！请检查容器状态和数据库连接信息" >> "$LOG_FILE"
    exit 1
fi

echo "----------------------------------------" >> "$LOG_FILE"

exit 0
