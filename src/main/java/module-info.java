module org.example.unishpere {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens org.example.unishpere to javafx.fxml;
    exports org.example.unishpere;
}