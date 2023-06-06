import { JobCodeParentsPipe } from './job-code-parents.pipe';
import { mockJobCodesParents } from "@test/resource/mock-data"

describe("JobCodeParentsPipe", () => {

  it("create an instance", () => {
    const pipe = new JobCodeParentsPipe();
    expect(pipe).toBeTruthy();
  });

  it("should transform correctly", () => {
    const pipe = new JobCodeParentsPipe();
    expect(pipe.transform(mockJobCodesParents[2])).toEqual("13-0000 Business and Financial Operations Occupations")
  })
});
