package PDollar;

public class DetailedInformation {

	public static String getDetailedInformation(String a) {
		String result = null;

		if (a.startsWith("battery")) {
			result = "��ʾ���ع���״̬��ָʾ�ƣ���ͨ���ź����𣬷�����������Ϩ�����������������Ӧ������鷢����������";
		} else if (a.startsWith("breakDisks")) {
			result = "��ʾɲ����Ƭĥ�������ָʾ�ơ���������´˵�Ϩ�𣬵���ʱ��ʾ����Ӧ��ʱ����ɲ��Ƭ���޸���Ϩ��";
		} else if (a.startsWith("engineOil")) {
			result = "��ʾ����������ѹ����ָʾ�ƣ���������ʱ��ʾ��ϵͳʧȥѹ������������©����ʱ������ͣ���رշ��������м��";
		} else if (a.startsWith("waterTempurature")) {
			result = "��ʾ��������ȴҺ�¶ȹ��ߵ�ָʾ�ƣ����˵Ƶ�������ʱ��Ӧ��ʹͣ�����رշ�����������ȴ�������¶Ⱥ��ټ�����ʻ";
		} else if (a.startsWith("airBag")) {
			result = "��ʾ��ȫ������������״̬��ָʾ�ƣ���ͨ���ź������Լ3-4���Ϩ�𣬱�ʾϵͳ����������������ʾϵͳ���ڹ���";
		} else if (a.startsWith("engine")) {
			result = "����������״̬��ָʾ�ƣ���ͨ���ź������Լ3-5���Ϩ�𣬷���������������������ʾ���������ϣ��輰ʱ���м��";
		} else if (a.startsWith("fuel")) {
			result = "��ʾȼ�Ͳ����ָʾ�ƣ��õ�����ʱ����ʾȼ�ͼ����ľ���һ��Ӹõ�����ȼ�ͺľ�֮ǰ������������ʻ50��������";
		} else if (a.startsWith("door")) {
			result = "��ʾ�����Ƿ���ȫ�رյ�ָʾ�ƣ����Ŵ򿪻�δ�ܹر�ʱ����Ӧ��ָʾ��������ʾ��������δ�غã����Źرպ�Ϩ��";
		} else if (a.startsWith("seatBelt")) {
			result = "��ʾ��ȫ��״̬��ָʾ�ƣ����ճ��Ͳ�ͬ�ƻ��������������ʾ����ֱ��ϵ�ð�ȫ����Ϩ��";
		} else if (a.startsWith("handbreak")) {
			result = "פ���ƶ��ֱ�����ʱ���˳���������ɲ������ʱ����ָʾ���Զ�Ϩ��";
		} else if (a.startsWith("ABS")) {
			result = "��ͨ���ź������Լ3-4���Ϩ�𣬱�ʾϵͳ��������������ʱ��ʾϵͳ���ϡ�";
		}

		return result;
	}

}
