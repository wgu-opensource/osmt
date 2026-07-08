import { PublishStatus } from '../PublishStatus';
import { IStringListUpdate } from '../richskill/ApiSkillUpdate';
import { KeywordCount, KeywordType } from '../richskill/ApiSkill';

export interface ICollection {
  archiveDate?: Date;
  author?: string;
  creationDate?: Date;
  creator: string;
  id: string;
  name: string;
  description?: string;
  workspaceOwner?: string;
  publishDate?: Date;
  skills: string[];
  skillKeywords?: Map<KeywordType, KeywordCount[]>;
  status: PublishStatus;
  updateDate?: Date;
  uuid: string;
  credentialEngineUrl?: string;
}

export class ApiCollection {
  archiveDate?: Date;
  author?: string;
  creationDate?: Date;
  creator: string;
  id: string;
  name: string;
  description?: string;
  publishDate?: Date;
  workspaceOwner?: string;
  skills: string[];
  skillKeywords?: Map<KeywordType, KeywordCount[]>;
  status: PublishStatus;
  updateDate?: Date;
  uuid: string;
  credentialEngineUrl?: string;

  constructor({
    archiveDate,
    author,
    creationDate,
    creator,
    id,
    name,
    description,
    workspaceOwner,
    publishDate,
    skills,
    skillKeywords,
    status,
    updateDate,
    uuid,
    credentialEngineUrl,
  }: ICollection) {
    this.archiveDate = archiveDate;
    this.author = author;
    this.creationDate = creationDate;
    this.creator = creator;
    this.id = id;
    this.name = name;
    this.description = description;
    this.workspaceOwner = workspaceOwner;
    this.publishDate = publishDate;
    this.skills = skills;
    this.skillKeywords = skillKeywords;
    this.status = status;
    this.updateDate = updateDate;
    this.uuid = uuid;
    this.credentialEngineUrl = credentialEngineUrl;
  }
}

export interface ICollectionUpdate {
  name?: string;
  description?: string;
  status?: PublishStatus;
  author?: string;
  skills?: IStringListUpdate;
}

export class ApiCollectionUpdate {
  name?: string;
  description?: string;
  status?: PublishStatus;
  author?: string;
  skills?: IStringListUpdate;
  workSpaceOwner?: string;

  constructor({
    name,
    description,
    status,
    author,
    skills,
  }: ICollectionUpdate) {
    this.name = name;
    this.description = description;
    this.status = status;
    this.author = author;
    this.skills = skills;
  }
}
