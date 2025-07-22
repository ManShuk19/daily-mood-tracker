import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';
import { MoodType } from 'app/shared/model/enumerations/mood-type.model';

export interface IMoodEntry {
  id?: number;
  date?: dayjs.Dayjs;
  mood?: keyof typeof MoodType;
  user?: IUser;
}

export const defaultValue: Readonly<IMoodEntry> = {};
