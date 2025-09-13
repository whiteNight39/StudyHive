INSERT INTO Role (roleId, roleName, roleDescription, roleStatus, roleCreatedAt)
VALUES
    (gen_random_uuid(), 'SUPER_ADMIN', 'Has full system access and control over all features', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'USER', 'General platform user with ability to join/create rooms depending on credits', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'OWNER', 'Creator of a room, has ultimate authority within the room', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'ADMIN', 'Appointed by the Owner, assists in maintaining and managing the room', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'TUTOR', 'Provides educational support and guidance within rooms', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'MEMBER', 'Standard participant within a study room', 'ACTIVE', NOW());

INSERT INTO Privilege (privilegeId, privilegeName, privilegeDescription, privilegeStatus, privilegeCreatedAt)
VALUES
    (gen_random_uuid(), 'Update|Room', 'Update room details', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Delete|Room', 'Delete room', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Add|Member', 'Add member to room', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Assign|Role', 'Assign roles to members', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Kick|Member', 'Remove member from room', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Mute|Member', 'Mute a member', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Send|Group|Message', 'Send message to group', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Edit|Own|Group|Message', 'Edit own group messages', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Delete|Group|Message', 'Delete group messages', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Add|Note', 'Create a note', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Edit|Own|Note', 'Edit own note', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Delete|Note', 'Delete note', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Create|Session', 'Create session', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Update|Session', 'Update session', 'ACTIVE', NOW()),
    (gen_random_uuid(), 'Delete|Session', 'Delete session', 'ACTIVE', NOW());

INSERT INTO RolePrivilege (rolePrivilegeId, rolePrivilegeRoleId, rolePrivilegePrivilegeId, rolePrivilegeCreatedAt)
SELECT gen_random_uuid(), r.roleId, p.privilegeId, NOW()
FROM Role r
         CROSS JOIN Privilege p
WHERE r.roleName = 'SUPER_ADMIN';


INSERT INTO RolePrivilege (rolePrivilegeId, rolePrivilegeRoleId, rolePrivilegePrivilegeId, rolePrivilegeCreatedAt)
SELECT gen_random_uuid(), r.roleId, p.privilegeId, NOW()
FROM Role r
         CROSS JOIN Privilege p
WHERE r.roleName = 'OWNER';


INSERT INTO RolePrivilege (
    rolePrivilegeId,
    rolePrivilegeRoleId,
    rolePrivilegePrivilegeId,
    rolePrivilegeCreatedAt
)
SELECT
    gen_random_uuid(),
    r.roleId,
    p.privilegeId,
    NOW()
FROM
    role r
JOIN
    privilege p
    ON p.privilegeName IN (
        'Add|Member',
        'Kick|Member',
        'Mute|Member',
        'Send|Group|Message',
        'Edit|Own|Group|Message',
        'Delete|Group|Message',
        'Add|Note',
        'Edit|Own|Note',
        'Delete|Note'
    )
WHERE
    r.roleName = 'ADMIN';


INSERT INTO RolePrivilege (
    rolePrivilegeId,
    rolePrivilegeRoleId,
    rolePrivilegePrivilegeId,
    rolePrivilegeCreatedAt
)
SELECT
    gen_random_uuid(),
    r.roleId,
    p.privilegeId,
    NOW()
FROM
    role r
JOIN
    privilege p
    ON p.privilegeName IN (
        'Send|Group|Message',
        'Edit|Own|Group|Message',
        'Delete|Group|Message',
        'Add|Note',
        'Edit|Own|Note',
        'Delete|Note',
        'Create|Session',
        'Update|Session',
        'Delete|Session'
    )
WHERE
    r.roleName = 'TUTOR';



INSERT INTO RolePrivilege (
    rolePrivilegeId,
    rolePrivilegeRoleId,
    rolePrivilegePrivilegeId,
    rolePrivilegeCreatedAt
)
SELECT
    gen_random_uuid(),
    r.roleId,
    p.privilegeId,
    NOW()
FROM
    role r
JOIN
    privilege p
    ON p.privilegeName IN (
        'Send|Group|Message',
        'Edit|Own|Group|Message',
        'Delete|Group|Message',
        'Add|Note',
        'Edit|Own|Note',
        'Delete|Note'
    )
WHERE
    r.roleName = 'MEMBER';
