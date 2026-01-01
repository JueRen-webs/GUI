package fmsGUI;

import java.time.LocalDateTime;

// [重要概念]: extends Flight 表示它继承了 Flight 类
// 意味着 CargoFlight 自动拥有了 Flight 的所有属性 (航班号、起飞时间等)
public class CargoFlight extends Flight {
    private double cargoCapacity; // 独有属性: 载货量 (公斤)

    // 构造方法
    public CargoFlight(String flightNumber, String origin, String destination, 
                       LocalDateTime departureTime, LocalDateTime arrivalTime, 
                       Aircraft aircraft, double cargoCapacity) {
        // super(...) 调用父类 (Flight) 的构造方法，把通用数据传给父类处理
    		super(flightNumber, origin, destination, departureTime, arrivalTime, aircraft, 0);
        this.cargoCapacity = cargoCapacity; // 自己处理这个独有的属性
    }

    // 获取载货量
    public double getCargoCapacity() { return cargoCapacity; }
    // 修改载货量
    public void setCargoCapacity(double cargoCapacity) { this.cargoCapacity = cargoCapacity; }

    // [重要概念]: Polymorphism (多态)
    // 重写 toString 方法。当打印 CargoFlight 时，会比普通 Flight 多显示载货信息
    @Override
    public String toString() {
        // super.toString() 拿到父类的文字描述，然后我们在后面加上载货量信息
        // %.0f 表示保留 0 位小数的浮点数
        return super.toString() + String.format(" | [Cargo: %.0fkg]", cargoCapacity);
    }
}