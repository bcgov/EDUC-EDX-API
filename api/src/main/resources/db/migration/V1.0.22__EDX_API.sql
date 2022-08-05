ALTER TABLE EDX_ACTIVATION_CODE
    ADD COLUMN EDX_USER_ID UUID;

ALTER TABLE EDX_ACTIVATION_CODE
    ADD CONSTRAINT FK_EDX_ACTIVATION_CODE_EDX_USER_ID FOREIGN KEY (EDX_USER_ID) REFERENCES EDX_USER (EDX_USER_ID);