INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'MID',
        'You have not declared any middle names. Please provide all other given names or middle names that you may have previously used or advise if you have never used any other given names.',
        'MOREINFO', 'IDIR/JOCOX', to_date('2021-04-28 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2021-04-28 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
