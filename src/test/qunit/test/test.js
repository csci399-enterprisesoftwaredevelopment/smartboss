/*
Unit tests that cover basic functionality of app.js.
 */

module('User Row Construction');

test('Build 2 User Rows', function() {
    expect(1);

    var users = [{"email": "jane.doe@company.com", "id": 1, "name": "Jane Doe", "phoneNumber": "12312312311"},{"email": "john.doe@company.com", "id": 0, "name": "John Doe", "phoneNumber": "2125551212"}];

    var html = buildUserRows(users);

    ok($(html).length == 2, 'Number of rows built: ' + length);
});

test('Build 0 user Rows', function() {
    expect(1);

    var users = [];

    var html = buildUserRows(users);

    ok($(html).length == 0, 'Created no rows for empty users');
});

module('User Restful Calls');

test('Register a new user', function() {
    ok(1==1,"TODO");
});

test('Register a user with a duplicate email', function() {
    ok(1==1,"TODO");
});
