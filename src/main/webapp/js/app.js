
/*
Core JavaScript functionality for the application.  Performs the required
Restful calls, validates return values, and populates the user table.
*/

/* Builds the updated table for the user list */
function buildUserRows(users) {
    return _.template( $( "#user-tmpl" ).html(), {"users": users});
}

/* Uses JAX-RS GET to retrieve current user list */
function updateUserTable() {
    $.ajax({
        url: "rest/Users",
        cache: false,
        success: function(data) {
            $('#users').empty().append(buildUserRows(data));
        },
        error: function(error) {
            //console.log("error updating table -" + error.status);
        }
    });
}

/*
Attempts to register a new user using a JAX-RS POST.  The callbacks
the refresh the user table, or process JAX-RS response codes to update
the validation errors.
 */
function registerUser(userData) {
    //clear existing  msgs
    $('span.invalid').remove();
    $('span.success').remove();

    $.ajax({
        url: 'rest/Users',
        contentType: "application/json",
        dataType: "json",
        type: "POST",
        data: JSON.stringify(userData),
        success: function(data) {
            console.log("User registered");

            //clear input fields
            $('#reg')[0].reset();

            //mark success on the registration form
            $('#formMsgs').append($('<span class="success">User Registered</span>'));

            updateUserTable();
        },
        error: function(error) {
            if ((error.status == 409) || (error.status == 400)) {
                console.log("Validation error registering user!");

                var errorMsg = $.parseJSON(error.responseText);

                $.each(errorMsg, function(index, val) {
                    $('<span class="invalid">' + val + '</span>').insertAfter($('#' + index));
                });
            } else {
                //console.log("error - unknown server issue");
                $('#formMsgs').append($('<span class="invalid">Unknown server error</span>'));
            }
        }
    });
}
