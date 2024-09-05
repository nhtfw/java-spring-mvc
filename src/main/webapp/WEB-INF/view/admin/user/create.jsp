<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
            <%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="utf-8" />
                    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
                    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
                    <meta name="description" content="Hỏi Dân IT - Dự án laptopshop" />
                    <meta name="author" content="Hỏi Dân IT" />
                    <title>Create user - Hỏi Dân IT</title>
                    <link href="/css/styles.css" rel="stylesheet" />
                    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js"
                        crossorigin="anonymous"></script>
                    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
                    <script>
                        $(document).ready(() => { //sau khi page load xong
                            const avatarFile = $("#avatarFile"); //lay bien voi id = avatarFile
                            avatarFile.change(function (e) { //khi file bi thay doi
                                const imgURL = URL.createObjectURL(e.target.files[0]); //lay duong link url bang web api co' san~
                                $("#avatarPreview").attr("src", imgURL); //them gia tri cho property src(nguon anh)
                                $("#avatarPreview").css({ "display": "block" }); //style display block != none
                            });
                        });
                    </script>
                </head>

                <body class="sb-nav-fixed">
                    <jsp:include page="../layout/header.jsp" />
                    <div id="layoutSidenav">
                        <jsp:include page="../layout/sidebar.jsp" />
                        <div id="layoutSidenav_content">
                            <main>
                                <div class="container-fluid px-4">
                                    <h1 class="mt-4">Manage users</h1>
                                    <ol class="breadcrumb mb-4">
                                        <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                                        <li class="breadcrumb-item active">Users</li>
                                    </ol>
                                    <div class="mt-5">
                                        <div class="row">
                                            <div class="col-md-6 col-12 mx-auto">
                                                <form:form method="post" action="/admin/user/create"
                                                    modelAttribute="newUser" class="row" enctype="multipart/form-data">
                                                    <div class="mb-3 col-12 col-md-6">
                                                        <c:set var="errorEmail">
                                                            <form:errors path="email" cssClass="invalid-feedback" />
                                                        </c:set>
                                                        <label for="exampleInputEmail1" class="form-label">Email
                                                            address</label>
                                                        <form:input type="email"
                                                            class="form-control ${not empty errorEmail ? 'is-invalid' : ''}"
                                                            id="exampleInputEmail1" path="email" />
                                                        ${errorEmail}
                                                    </div>
                                                    <div class="mb-3 col-12 col-md-6">
                                                        <c:set var="errorPassword">
                                                            <form:errors path="password" cssClass="invalid-feedback" />
                                                        </c:set>
                                                        <label for="exampleInputPassword1"
                                                            class="form-label">Password</label>
                                                        <form:input type="password"
                                                            class="form-control ${not empty errorPassword ? 'is-invalid' : ''}"
                                                            id="exampleInputPassword1" path="password" />
                                                        ${errorPassword}
                                                    </div>
                                                    <div class="mb-3 col-12 col-md-6">
                                                        <label for="exampleInputPhoneNumber" class="form-label">Phone
                                                            number</label>
                                                        <form:input type="number" class="form-control"
                                                            id="exampleInputPhoneNumber" path="phone" />
                                                    </div>
                                                    <div class="mb-3 col-12 col-md-6">
                                                        <c:set var="errorFullname">
                                                            <form:errors path="fullName" cssClass="invalid-feedback" />
                                                        </c:set>
                                                        <label for="exampleInputFullname" class="form-label">Full
                                                            name</label>
                                                        <form:input type="text"
                                                            class="form-control ${not empty errorFullname ? 'is-invalid' : ''}"
                                                            id="exampleInputFullname" path="fullName" />
                                                        ${errorFullname}
                                                    </div>
                                                    <div class="mb-3 col-12">
                                                        <label for="exampleInputAddress"
                                                            class="form-label">Address</label>
                                                        <form:input type="text" class="form-control"
                                                            id="exampleInputAddress" path="address" />
                                                    </div>
                                                    <div class="mb-3 col-12 col-md-6">
                                                        <label for="formSelectRole" class="form-label">Role</label>
                                                        <form:select class="form-select" id="formSelectRole"
                                                            path="role.name">
                                                            <form:option value="ADMIN">ADMIN</form:option>
                                                            <form:option value="USER">USER</form:option>
                                                        </form:select>
                                                    </div>
                                                    <div class="mb-3 col-12 col-md-6">
                                                        <label for="avatarFile" class="form-label">Avatar</label>
                                                        <input class="form-control" type="file" id="avatarFile"
                                                            accept=".png, .jpg, .jpeg" name="imageFile" />
                                                    </div>
                                                    <div class="col-12 mb-3">
                                                        <img style="max-height: 250px; display: none;"
                                                            alt="avatar preview" id="avatarPreview" />
                                                    </div>
                                                    <div class="col-12 mb-5">
                                                        <button type="submit" class="btn btn-primary">Submit</button>
                                                    </div>
                                                </form:form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </main>
                            <jsp:include page="../layout/footer.jsp" />
                        </div>
                    </div>
                    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
                        crossorigin="anonymous"></script>
                    <script src="js/scripts.js"></script>
                </body>

                </html>