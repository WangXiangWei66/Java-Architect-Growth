package abstractfactory;


public class ModernFactory extends AbastractFactory {

    @Override
    Vehicle createVehicle() {
        return new Car();
    }

    @Override
    Weapon createWeapon() {
        return new Ak47();
    }

    @Override
    Food createFood() {
        return new Bread();
    }
}
