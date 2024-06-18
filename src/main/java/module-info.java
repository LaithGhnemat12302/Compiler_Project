module com.example.laith_1200610_compiler {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.laith_1200610_compiler to javafx.fxml;
    exports com.example.laith_1200610_compiler;
}