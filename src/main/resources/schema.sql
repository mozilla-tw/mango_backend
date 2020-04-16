CREATE TABLE worker (
  id serial PRIMARY KEY,
  task VARCHAR(50),
  data TEXT,
  status VARCHAR(10),
  create_ts timestamptz DEFAULT NOW(),
  update_ts timestamptz DEFAULT NOW()
);

CREATE TABLE push (
  id serial PRIMARY KEY,
  moz_msg_id VARCHAR(50),
  moz_client_id TEXT,
  moz_msg_batch VARCHAR(50),
  fcm_msg_id TEXT,
  error TEXT,
  create_ts timestamptz DEFAULT NOW(),
  update_ts timestamptz DEFAULT NOW()
);
