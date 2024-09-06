package vn.hoidanit.laptopshop.service.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = StrongPasswordValidator.class) // đây là anotation với mục địch validate dữ liệu
// để 'định nghĩa' cơ chế hoạt động của anotation này, ta truyền vào 1 class đặc
// biệt, trong trường hợp này là StrongPasswordValidator
@Target({ ElementType.METHOD, ElementType.FIELD }) // phạm vi hđ, ở đây là field, không phải cả một class như
                                                   // @entity,@controller...
@Retention(RetentionPolicy.RUNTIME) // khi chương trình chạy thì nó cũng chạy
@Documented
public @interface StrongPassword {
    String message() default "Must be 8 characters long and combination of uppercase letters, lowercase letters, numbers, special characters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
