-- CREATE TABLE `accounts`
-- (
--     `id` varchar(64) NOT NULL,
--     PRIMARY KEY (`id`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `threads`
(
    `id`         varchar(64) NOT NULL,
    `created_at` TEXT        NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `members`
(
    `thread_id`  varchar(64) NOT NULL,
    `account_id` varchar(64) NOT NULL,
    `created_at` TEXT        NOT NULL,
    PRIMARY KEY (`thread_id`, `account_id`),
    FOREIGN KEY (`thread_id`) REFERENCES threads (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `messages`
(
    `id`         varchar(64) NOT NULL,
    `thread_id`  varchar(64) NOT NULL,
    `account_id` varchar(64) NOT NULL,
    `text`       TEXT NOT NULL,
    `created_at` TEXT        NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`thread_id`) REFERENCES threads (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
