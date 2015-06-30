package PDollar;

public class DetailedInformation {

	public static String getDetailedInformation(String a) {
		String result = null;

		if (a.startsWith("battery")) {
			result = "显示蓄电池工作状态的指示灯，接通电门后亮起，发动机启动后熄灭。如果不亮或长亮不灭应立即检查发动机及电器";
		} else if (a.startsWith("breakDisks")) {
			result = "显示刹车盘片磨损情况的指示灯。正常情况下此灯熄灭，点亮时提示车主应及时更换刹车片，修复后熄灭";
		} else if (a.startsWith("engineOil")) {
			result = "显示发动机机油压力的指示灯，本灯亮起时表示润滑系统失去压力，可能有渗漏，此时需立即停车关闭发动机进行检查";
		} else if (a.startsWith("waterTempurature")) {
			result = "显示发动机冷却液温度过高的指示灯，若此灯点亮报警时，应即使停车并关闭发动机，待冷却至正常温度后再继续行驶";
		} else if (a.startsWith("airBag")) {
			result = "显示安全气囊正常工作状态的指示灯，接通电门后点亮，约3-4秒后熄灭，表示系统正常，不亮或常量表示系统存在故障";
		} else if (a.startsWith("engine")) {
			result = "发动机工作状态的指示灯，接通电门后点亮，约3-5秒后熄灭，发动机正常。不亮或长亮表示发动机故障，需及时进行检查";
		} else if (a.startsWith("fuel")) {
			result = "提示燃油不足的指示灯，该灯亮起时。表示燃油即将耗尽，一般从该灯亮起到燃油耗尽之前，车辆还能行驶50公里左右";
		} else if (a.startsWith("door")) {
			result = "显示车门是否完全关闭的指示灯，车门打开或未能关闭时。相应的指示灯亮起，提示车主车门未关好，车门关闭后熄灭";
		} else if (a.startsWith("seatBelt")) {
			result = "显示安全带状态的指示灯，按照车型不同灯会亮起数秒进行提示，或直到系好安全带才熄灭";
		} else if (a.startsWith("handbreak")) {
			result = "驻车制动手柄拉起时，此车点亮，手刹被放下时，该指示灯自动熄灭";
		} else if (a.startsWith("ABS")) {
			result = "接通电门后点亮，约3-4秒后熄灭，表示系统正常，不亮或常亮时表示系统故障。";
		}

		return result;
	}

}
