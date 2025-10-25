package abstractfactory;

public class MagicFactory extends AbastractFactory{

    @Override
    Vehicle createVehicle() {
        return new Broom();
    }

    @Override
    Food createFood() {
        return new MushRoom();
    }

    @Override
    Weapon createWeapon() {
        return new MagicStick();
    }
}
