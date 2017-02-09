function makeAdmin(isAdminCheckBox) {
    if (isAdminCheckBox.checked == true) {
        isAdminCheckBox.value=true;
    } else {
        isAdminCheckBox.value=false;
    }
}