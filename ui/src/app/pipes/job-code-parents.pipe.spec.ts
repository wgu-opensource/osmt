import { JobCodeParentsPipe } from './job-code-parents.pipe';
import { ApiJobCodeUpdate, IJobCode } from "../metadata/job-codes/Jobcode"

describe("JobCodeParentsPipe", () => {

  it("create an instance", () => {
    const pipe = new JobCodeParentsPipe();
    expect(pipe).toBeTruthy();
  });

  it("should transform correctly", () => {
    const pipe = new JobCodeParentsPipe();
    const parents: IJobCode[] = [
      {
        id: 111,
        code: "13-2010",
        targetNodeName: "Accountants and Auditors",
        frameworkName: "bls",
        level: "Broad",
      },
      {
        id: 110,
        code: "13-2000",
        targetNodeName: "Financial Specialists",
        frameworkName: "bls",
        level: "Minor"
      },
      {
        id: 74,
        code: "13-0000",
        targetNodeName: "Business and Financial Operations Occupations",
        frameworkName: "bls",
        level: "Major"
      }
    ]
    expect(pipe.transform(parents)).toEqual("13-0000 Business and Financial Operations Occupations,13-2000 Financial Specialists,13-2010 Accountants and Auditors")
  })
});
