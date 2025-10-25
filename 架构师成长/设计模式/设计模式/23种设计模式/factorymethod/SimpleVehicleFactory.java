package factorymethod;

public class SimpleVehicleFactory {

    public Car createCar() {
        return new Car();
    }

    public Broom createBroom() {
        return new Broom();
    }
}
