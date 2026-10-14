-- Initialize isolated PostgreSQL databases for FinSight AI microservices
CREATE DATABASE finsight_auth;
CREATE DATABASE finsight_financial_data;
CREATE DATABASE finsight_reconciliation;
CREATE DATABASE finsight_fraud;
CREATE DATABASE finsight_audit;
CREATE DATABASE finsight_insights;
CREATE DATABASE finsight_ai;

GRANT ALL PRIVILEGES ON DATABASE finsight_auth TO finsight_user;
GRANT ALL PRIVILEGES ON DATABASE finsight_financial_data TO finsight_user;
GRANT ALL PRIVILEGES ON DATABASE finsight_reconciliation TO finsight_user;
GRANT ALL PRIVILEGES ON DATABASE finsight_fraud TO finsight_user;
GRANT ALL PRIVILEGES ON DATABASE finsight_audit TO finsight_user;
GRANT ALL PRIVILEGES ON DATABASE finsight_insights TO finsight_user;
GRANT ALL PRIVILEGES ON DATABASE finsight_ai TO finsight_user;
