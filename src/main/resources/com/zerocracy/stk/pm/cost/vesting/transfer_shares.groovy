/**
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.stk.pm.cost.vesting

import com.jcabi.xml.XML
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Equity
import com.zerocracy.pm.cost.Vesting
import com.zerocracy.pm.staff.Roles

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Make payment')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String login = claim.param('login')
  int minutes = Integer.parseInt(claim.param('minutes'))
  Roles roles = new Roles(project).bootstrap()
  if (!roles.hasAnyRole(login)) {
    return
  }
  Cash cash
  if (claim.hasParam('cash')) {
    cash = new Cash.S(claim.param('cash'))
  } else {
    cash = Cash.ZERO
  }
  Vesting vesting = new Vesting(project).bootstrap()
  if (vesting.exists(login)) {
    Cash reward = (vesting.rate(login).mul(minutes) / 60).add(cash.mul(-1L))
    new Equity(project).bootstrap().add(login, reward)
    new ClaimOut()
      .type('Equity transferred')
      .param('login', login)
      .param('job', job)
      .param('reward', reward)
      .param('vesting_rate', vesting.rate(login))
      .param('minutes', minutes)
      .param('cash', cash)
      .postTo(project)
    new ClaimOut()
      .type('Notify user')
      .param('login', login)
      .param(
        'message',
        new Par(
          'You earned %s of new share in %s for %s'
        ).say(reward, project.pid(), job)
      )
      .postTo(project)
    new ClaimOut()
      .type('Notify project')
      .param(
        'message',
        new Par(
          'We just transferred %s of share for %s to @%s'
        ).say(reward, job, login)
      )
      .postTo(project)
  }
}
